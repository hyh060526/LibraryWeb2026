package org.example.web;

import javax.imageio.ImageIO;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Random;

/**
 * 登录验证码：4 位数字/字母，写入 session。
 */
@WebServlet("/api/captcha")
public class CaptchaServlet extends HttpServlet {

    public static final String SESSION_CAPTCHA = "captchaCode";

    private static final char[] CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789".toCharArray();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        int width = 110;
        int height = 40;
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();
        Random random = new Random();
        g.setColor(new Color(245, 247, 250));
        g.fillRect(0, 0, width, height);

        StringBuilder code = new StringBuilder();
        g.setFont(new Font("Arial", Font.BOLD, 24));
        for (int i = 0; i < 4; i++) {
            char c = CHARS[random.nextInt(CHARS.length)];
            code.append(c);
            g.setColor(new Color(40 + random.nextInt(80), 40 + random.nextInt(80), 120 + random.nextInt(80)));
            g.drawString(String.valueOf(c), 18 + i * 22, 28);
        }
        for (int i = 0; i < 8; i++) {
            g.setColor(new Color(180, 180, 180));
            g.drawLine(random.nextInt(width), random.nextInt(height),
                    random.nextInt(width), random.nextInt(height));
        }
        g.dispose();

        HttpSession session = req.getSession(true);
        session.setAttribute(SESSION_CAPTCHA, code.toString());

        resp.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        resp.setContentType("image/png");
        ImageIO.write(image, "png", resp.getOutputStream());
    }
}
