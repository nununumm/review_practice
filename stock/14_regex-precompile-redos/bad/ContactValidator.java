import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

@Service
public class ContactValidator {

    public List<ContactForm> extractValid(List<ContactForm> forms) {
        List<ContactForm> valid = new ArrayList<>();

        for (ContactForm form : forms) {
            Pattern emailPattern = Pattern.compile("^(.+)+@.+$");
            boolean emailOk = emailPattern.matcher(form.getEmail()).matches();

            boolean phoneOk = form.getPhone().matches("^0\\d{1,4}-?\\d{1,4}-?\\d{4}$");

            try {
                if (emailOk && phoneOk) {
                    valid.add(form);
                }
            } catch (Exception e) {
                valid.add(form);
            }
        }

        return valid;
    }
}
