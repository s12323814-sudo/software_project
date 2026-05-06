package admain;

import java.util.Objects;

/**
 * يمثل هذا الكلاس حساب مستخدم داخل النظام.
 *
 * <p>يحتوي على معلومات أساسية مثل اسم المستخدم، كلمة المرور (بشكل مشفر)،
 * البريد الإلكتروني، ودور المستخدم (مثل ADMIN أو USER).</p>
 *
 * <p>يتم التحقق من صحة القيم عند التعيين لضمان سلامة البيانات:</p>
 * <ul>
 *   <li>اسم المستخدم يجب أن يكون على الأقل 3 أحرف.</li>
 *   <li>كلمة المرور المشفرة يجب ألا تكون فارغة أو null.</li>
 *   <li>البريد الإلكتروني يجب أن يحتوي على '@'.</li>
 * </ul>
 *
 * <p>رقم الحساب (accountId) ثابت ولا يمكن تغييره، ويُستخدم لتمييز كل حساب بشكل فريد.</p>
 */
public class Accounty {

    private final int accountId;
    private String username;
    private String passwordHash;
    private String email;
    private Role_y role;

    /**
     * ينشئ حساب مستخدم جديد.
     *
     * @param accountId رقم فريد للحساب
     * @param username اسم المستخدم (يجب أن يكون 3 أحرف على الأقل)
     * @param passwordHash كلمة المرور المشفرة (يجب ألا تكون فارغة)
     * @param email البريد الإلكتروني (يجب أن يحتوي على '@')
     * @param role دور المستخدم (ADMIN أو USER)
     * @throws IllegalArgumentException في حال إدخال قيم غير صالحة
     */
    public Account_y(int accountId, String username, String passwordHash, String email, Role_y role) {
        this.accountId = accountId;
        setUsername(username);
        setPasswordHash(passwordHash);
        setEmail(email);
        this.role = role;
    }

    /**
     * @return رقم الحساب
     */
    public int getAccountId() {
        return accountId;
    }

    /**
     * @return اسم المستخدم
     */
    public String getUsername() {
        return username;
    }

    /**
     * @return كلمة المرور المشفرة
     */
    public String getPasswordHash() {
        return passwordHash;
    }

    /**
     * @return البريد الإلكتروني
     */
    public String getEmail() {
        return email;
    }

    /**
     * @return دور المستخدم
     */
    public Role_y getRole() {
        return role;
    }

    /**
     * تعيين اسم المستخدم.
     *
     * @param username اسم المستخدم
     * @throws IllegalArgumentException إذا كان null أو أقل من 3 أحرف
     */
    public void setUsername(String username) {
        if (username == null || username.length() < 3) {
            throw new IllegalArgumentException("Invalid username");
        }
        this.username = username;
    }

    /**
     * تعيين كلمة المرور المشفرة.
     *
     * @param passwordHash كلمة المرور المشفرة
     * @throws IllegalArgumentException إذا كانت null أو فارغة
     */
    public void setPasswordHash(String passwordHash) {
        if (passwordHash == null || passwordHash.isEmpty()) {
            throw new IllegalArgumentException("Invalid password hash");
        }
        this.passwordHash = passwordHash;
    }

    /**
     * تعيين البريد الإلكتروني.
     *
     * @param email البريد الإلكتروني
     * @throws IllegalArgumentException إذا كان غير صالح
     */
    public void setEmail(String email) {
        if (email == null || !email.contains("@")) {
            throw new IllegalArgumentException("Invalid email");
        }
        this.email = email;
    }

    /**
     * تعيين دور المستخدم.
     *
     * @param role الدور
     */
    public void setRole(Role_y role) {
        this.role = role;
    }

    /**
     * التحقق إذا كان المستخدم أدمن.
     *
     * @return true إذا كان ADMIN
     */
    public boolean isAdmin() {
        return role != null && role == Role_y.ADMIN;
    }

    /**
     * التحقق إذا كان المستخدم عادي.
     *
     * @return true إذا كان USER
     */
    public boolean isUser() {
        return role != null && role == Role_y.USER;
    }

    /**
     * إرجاع تمثيل نصي للكائن (بدون كلمة المرور لأسباب أمنية).
     *
     * @return نص يمثل الحساب
     */
    @Override
    public String toString() {
        return "Account{" +
                "id=" + accountId +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", role=" + role +
                '}';
    }

    /**
     * مقارنة هذا الكائن مع كائن آخر.
     *
     * @param o الكائن الآخر
     * @return true إذا كان لهما نفس accountId
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Account_y)) return false;
        Account_y account = (Account_y) o;
        return accountId == account.accountId;
    }

    /**
     * إنشاء hashCode بناءً على accountId.
     *
     * @return قيمة hash
     */
    @Override
    public int hashCode() {
        return Objects.hash(accountId);
    }
}
