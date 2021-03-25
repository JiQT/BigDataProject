import lombok.Data;
@Data
public class tutorial {
    private int id;
    private String name;
    private int age;


    public static void main(String[] args) {
        tutorial tutorial = new tutorial();
        tutorial.setId(0);
        tutorial.setName("");
        tutorial.setAge(0);
    }
}



