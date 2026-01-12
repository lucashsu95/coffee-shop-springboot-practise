package tw.edu.ntub.imd.birc.coffeeshop.exception.form;

public class InvalidFormNumberFormatException extends InvalidRequestFormatException {

    public InvalidFormNumberFormatException(NumberFormatException e) {
        super(e.getMessage().substring(18) + "不能轉換成數字");
    }
}

