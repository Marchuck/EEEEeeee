package pl.marchuck.eeeeeeee;


import com.example.SQLiteField;
import com.example.SQLitePrimaryKey;
import com.example.SQLiteTable;

/**
 * Project "Annotations101"
 * <p/>
 * Created by Lukasz Marczak
 * on 09.10.16.
 */
@SQLiteTable("MY_GENERATED_TABLE")
public class Pojo {

    @SQLiteField("uuid")
    @SQLitePrimaryKey
    private String uuid;

    @SQLiteField("message")
    private String message;

    @SQLiteField(value = "number", type = "INT")
    private int number;

    @SQLiteField("date")
    private String date;


    @SQLiteField(value = "age", type = "SHORT")
    private short age;

    public Pojo() {
    }

    public Pojo(String uuid, String message, int number, String date, short age) {
        this.uuid = uuid;
        this.message = message;
        this.number = number;
        this.date = date;
        this.age = age;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public short getAge() {
        return age;
    }

    public void setAge(short age) {
        this.age = age;
    }
}
