package su.asgor.service;

import org.joda.time.DateTime;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import su.asgor.dao.UserRepository;
import su.asgor.model.Category;
import su.asgor.model.Purchase;
import su.asgor.model.User;
import su.asgor.parser.generated.fz223.PurchaseNoticeStatusType;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.*;

@Service
@Transactional
public class MailService {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private Environment environment;
    @Autowired
    private PropertyService propertyService;
    private JavaMailSenderImpl javaMailSender;
    private final Logger log = LoggerFactory.getLogger(this.getClass());

	public void sendConfirmRegistration(String email, String verificationUrl) {
        send(email, "Подтверждение регистарции", "Чтобы подтвердить регистрацию перейдите по ссылке: "+verificationUrl);
    }

	public void sendPasswordRecovery(String email, String url){
		send(email, "Восстановление пароля", "Чтобы востановить пароль перейдите по ссылке: " + url);
	}

    public void notifyUsers(){
        for(User user: userRepository.findAll()){
            if (user.getNotifyFavsChange()!=null && user.getNotifyFavsChange()){
                List<Purchase> purchases = new ArrayList<>();
                for(Purchase purchase:user.getFavs()){
                    if(purchase.getSubmissionCloseDate()!=null){
                        DateTime tomorrow = new DateTime().plusDays(7);
                        DateTime purchaseDate = new DateTime(purchase.getSubmissionCloseDate().getTime());
                        if(purchaseDate.getYear()==tomorrow.getYear()&&
                                purchaseDate.getMonthOfYear()==tomorrow.getMonthOfYear()&&
                                purchaseDate.getDayOfMonth()==tomorrow.getDayOfMonth()&&purchaseDate.isAfterNow()){
                            purchases.add(purchase);
                        }
                    }
                }
                if(!purchases.isEmpty()){
                    sendSubmissionCloseNotification(user,purchases);
                }
            }
        }
    }

    public void sendNotification(List<Purchase> purchases){
        Map<User,String> messages = new HashMap<>();
        List<User> users = userRepository.findAll();
        for(Purchase purchase : purchases){
            for(User user : users){
                if(user.getFavs().contains(purchase)){
                    String message = "";
                    if(purchase.getStatus() == PurchaseNoticeStatusType.M)
                        message += "Была изменена избранная закупка";
                    if(purchase.getStatus() == PurchaseNoticeStatusType.I)
                        message += "Была отменена избранная закупка";
                    message += "\nЗакупка №" + purchase.getId() +"\n";
                    message += purchase.getName()+"\n";
                    message += "Заказчик: "+purchase.getCustomer().getName()+"\n";
                    message += "Url: " + getPath() + "/#/purchase/" + purchase.getId()+"\n\n";
                    send(user.getEmail(), "Изменения в избранных закупок", message);
                    continue;
                }
                if(!PurchaseService.isCompleted(purchase) && purchase.getStatus() == PurchaseNoticeStatusType.P &&
                        !Collections.disjoint(purchase.getCategories(), user.getSubscriptions())){
                    String categories = "";
                    for(Category category : purchase.getCategories()){
                        if(user.getSubscriptions().contains(category))
                            categories += category.getName()+", ";
                    }
                    String message = "";
                    message += "Добавлена закупка в категории: " + categories.substring(0,categories.length()-2);
                    message += "\nЗакупка №" + purchase.getId() +"\n";
                    message += purchase.getName()+"\n";
                    message += "Заказчик: "+purchase.getCustomer().getName()+"\n";
                    message += "Url: " + getPath() + "/#/purchase/" + purchase.getId()+"\n\n";
                    if(messages.containsKey(user)){
                        messages.get(user).concat(message);
                    }else{
                        messages.put(user, message);
                    }
                }
            }
        }

        for(Map.Entry<User,String> entry : messages.entrySet()){
            send(entry.getKey().getEmail(), "Изменения в избранных категориях", entry.getValue());
        }
    }

    public void sendSubmissionCloseNotification(User user, List<Purchase> purchases){
        String subject = "Приближение окончания срока подачи заявок избранных закупок";
        String message = "";
        for(Purchase purchase : purchases){
            message += "Закупка №" + purchase.getId() +"\n";
            message += purchase.getName()+"\n";
            message += "Заказчик: "+purchase.getCustomer().getName()+"\n";
            message += "Окончание подачи заявок: "+purchase.getSubmissionCloseDate()+"\n";
            message += "Url: " + getPath() + "/#/purchase/" + purchase.getId()+"\n\n";
        }
        send(user.getEmail(), subject, message);
    }

	private void send(String email, String subject, String message) {
        javaMailSender = new JavaMailSenderImpl();
        javaMailSender.setHost(propertyService.get("app.mail.host"));
        javaMailSender.setPort(Integer.valueOf(propertyService.get("app.mail.port")));
        javaMailSender.setUsername(propertyService.get("app.mail.username"));
        javaMailSender.setPassword(propertyService.get("app.mail.password"));
        javaMailSender.setProtocol("smtp");
        Properties props = new Properties();
        props.put("mail.smtp.ssl.enable","true");
        javaMailSender.setJavaMailProperties(props);

        log.info("sending mail to: " + email + ", Subject: "+subject);
        MimeMessage mail = javaMailSender.createMimeMessage();
        try {
            MimeMessageHelper helper = new MimeMessageHelper(mail, true);
            helper.setTo(email);
            helper.setFrom("test-zakupki@mail.ru");
            helper.setSubject(subject);
            helper.setText(message);
        } catch (MessagingException e) {
            e.printStackTrace();
        }
        javaMailSender.send(mail);
    }

    private String getPath(){
        try {
            return "http://"+InetAddress.getLocalHost().getHostAddress()+":"+environment.getProperty("server.port");
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
    }
}
