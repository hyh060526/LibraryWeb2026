package org.example.entity;

import org.example.annotation.Table;
import org.example.annotation.Transient;

@Table("bookcopy")
public class BookCopy extends Base {

    /** 所属图书外键 */
    private String bookId;

    /** 借阅状态, true 为已借出, false 为未借阅 */
    private boolean isBorrowed;

    /** 属于哪种书（不持久化） */
    @Transient
    private Book book;

    public String getBookId() {
        return bookId;
    }

    public void setBookId(String bookId) {
        this.bookId = bookId;
    }

    public boolean isBorrowed() {
        return isBorrowed;
    }

    public void setBorrowed(boolean borrowed) {
        isBorrowed = borrowed;
    }

    public boolean getIsBorrowed() {
        return isBorrowed;
    }

    public void setIsBorrowed(boolean borrowed) {
        isBorrowed = borrowed;
    }

    public Book getBook() {
        return book;
    }

    public void setBook(Book book) {
        this.book = book;
    }
}
