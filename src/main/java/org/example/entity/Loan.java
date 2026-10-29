package org.example.entity;

import org.example.annotation.Table;
import org.example.annotation.Transient;

import java.time.LocalDateTime;

@Table("loan")
public class Loan extends Base {

    /** 图书副本外键 */
    private String bookCopyId;

    /** 用户外键 */
    private String userId;

    /** 图书（不持久化） */
    @Transient
    private BookCopy bookCopy;

    /** 用户（不持久化） */
    @Transient
    private User user;

    /** 借书日期 */
    private LocalDateTime borrowDate;

    /** 还书日期 */
    private LocalDateTime returnDate;

    /** 应还日期（根据图书的 days 属性计算） */
    private LocalDateTime dueDate;

    /** 状态：未还、已还、超期 */
    private String status;

    public String getBookCopyId() {
        return bookCopyId;
    }

    public void setBookCopyId(String bookCopyId) {
        this.bookCopyId = bookCopyId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public BookCopy getBookCopy() {
        return bookCopy;
    }

    public void setBookCopy(BookCopy bookCopy) {
        this.bookCopy = bookCopy;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public LocalDateTime getBorrowDate() {
        return borrowDate;
    }

    public void setBorrowDate(LocalDateTime borrowDate) {
        this.borrowDate = borrowDate;
    }

    public LocalDateTime getReturnDate() {
        return returnDate;
    }

    public void setReturnDate(LocalDateTime returnDate) {
        this.returnDate = returnDate;
    }

    public LocalDateTime getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDateTime dueDate) {
        this.dueDate = dueDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
