package sa.store.retaildiscount.enums;

public enum CustomerTypeEnum {
    EMPLOYEE("EMPLOYEE"),
    AFFILIATE("AFFILIATE"),
    REGULAR("REGULAR");

    private final String name;

    CustomerTypeEnum(String name) {
        this.name = name;
    }

    public String getName() {

        if (name == null || name.isEmpty()) {
            return "REGULAR";
        }
        return name;

        }


    }
