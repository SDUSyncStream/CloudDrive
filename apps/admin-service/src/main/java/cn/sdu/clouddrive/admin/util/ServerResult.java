// cn.sdu.clouddrive.admin.utils.ServerResult.java
package cn.sdu.clouddrive.admin.util;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ServerResult<T> {
    private int code; // 状态码，例如 200 成功，400 失败，500 服务器错误
    private String message; // 提示信息
    private T data; // 返回的数据

    // 成功时的静态方法
    public static <T> ServerResult<T> ok(T data, String message) {
        return new ServerResult<>(200, message, data);
    }

    public static <T> ServerResult<T> ok(T data) {
        return ok(data, "操作成功");
    }

    public static <T> ServerResult<T> ok() {
        return ok(null, "操作成功");
    }

    // 失败时的静态方法
    public static <T> ServerResult<T> fail(String message) {
        return new ServerResult<>(400, message, null);
    }

    public static <T> ServerResult<T> fail(int code, String message) {
        return new ServerResult<>(code, message, null);
    }

    public static <T> ServerResult<T> fail() {
        return fail("操作失败");
    }
}