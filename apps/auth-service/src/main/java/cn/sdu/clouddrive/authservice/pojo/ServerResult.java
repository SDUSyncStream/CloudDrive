package cn.sdu.clouddrive.authservice.pojo;


import lombok.Data;

@Data
public class ServerResult<T>
{
    private Integer code;
    private String message;
    private T data;

    public static <T> ServerResult<T> ok(T data)
    {
        ServerResult<T> result = new ServerResult<>();
        result.setCode(200);
        result.setMessage("OK");
        result.setData(data);
        return result;
    }

    public static ServerResult<Void> ok()
    {
        return ok(null);
    }

    public static <T> ServerResult<T> error(Integer code, String message)
    {
        ServerResult<T> result = new ServerResult<>();
        result.setCode(code);
        result.setMessage(message);
        return result;
    }
}