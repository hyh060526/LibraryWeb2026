#!/usr/bin/env python3
"""把 LibraryDB 备份（meta.json + *.meta.json.gz + *.data.sql.gz）重建成可直接导入 MySQL 的 libraryadmin.sql。

备份为行分隔符 0x1E 的 SQL 元组格式；本脚本解析所有表，按父表->子表顺序输出
CREATE TABLE + INSERT 语句，并用 SET FOREIGN_KEY_CHECKS 包裹以规避外键顺序问题。
"""
import gzip
import json
import os

DB_DIR = os.path.join(os.path.dirname(__file__), "..", "LibraryDB")
OUT = os.path.join(os.path.dirname(__file__), "..", "db", "libraryadmin.sql")

# 父表在前，子表在后（bookcopy/loan/bookrecommend 依赖 user/book）
ORDER = {"user": 0, "book": 1, "bookcopy": 2, "loan": 3,
         "bookrecommend": 4, "log": 5, "knowledge": 6, "student": 7}


def load_meta():
    path = os.path.join(DB_DIR, "meta.json")
    with open(path, "r", encoding="utf-8") as f:
        return json.load(f)


def main():
    os.makedirs(os.path.dirname(OUT), exist_ok=True)
    meta = load_meta()
    rows = {}
    for obj in meta["Objects"]:
        name, uuid = obj["Name"], obj["UUID"]
        meta_file = os.path.join(DB_DIR, uuid + ".meta.json.gz")
        with gzip.open(meta_file, "rt", encoding="utf-8", errors="replace") as f:
            tm = json.load(f)
        ddl = tm["DDL"]
        fields = tm["Fields"]
        tuples = []
        data_files = tm.get("Data") or []
        if data_files:
            data_file = os.path.join(DB_DIR, data_files[0]["Filename"])
            with gzip.open(data_file, "rt", encoding="utf-8", errors="replace") as f:
                text = f.read()
            # 以 0x1E 记录分隔符切分，每个片段为一个 (..) 元组
            tuples = [p.strip() for p in text.split("\x1e") if p.strip().startswith("(")]
        rows[name] = {"ddl": ddl, "fields": fields, "tuples": tuples}

    out = []
    out.append("-- 图书馆管理系统数据库还原脚本 libraryadmin")
    out.append("-- 由 LibraryDB 备份自动生成（tools/restore_db.py）")
    out.append("SET NAMES utf8mb4;")
    out.append("SET FOREIGN_KEY_CHECKS = 0;")
    out.append("DROP DATABASE IF EXISTS `libraryadmin`;")
    out.append("CREATE DATABASE `libraryadmin` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;")
    out.append("USE `libraryadmin`;")
    out.append("")

    for name in sorted(rows, key=lambda n: ORDER.get(n, 99)):
        info = rows[name]
        out.append(info["ddl"].rstrip(";").rstrip() + ";")
        out.append("")
        if info["tuples"]:
            cols = ", ".join("`%s`" % c for c in info["fields"])
            out.append("INSERT INTO `%s` (%s) VALUES" % (name, cols))
            out.append(",\n".join(info["tuples"]) + ";")
            out.append("")

    out.append("SET FOREIGN_KEY_CHECKS = 1;")
    out.append("")

    with open(OUT, "w", encoding="utf-8") as f:
        f.write("\n".join(out))

    print("generated:", OUT)
    for name in sorted(rows, key=lambda n: ORDER.get(n, 99)):
        print("  table %-14s rows=%d" % (name, len(rows[name]["tuples"])))


if __name__ == "__main__":
    main()
