package su.asgor.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import su.asgor.dao.PurchaseRepository;
import su.asgor.dao.UserRepository;
import su.asgor.model.Purchase;
import su.asgor.model.User;

@Controller
@RequestMapping("/user")
public class UserController {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PurchaseRepository purchaseRepository;

    @RequestMapping(value = "/fav/{id}",method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<?> addToFav(@PathVariable String id, String email) {
        User user = userRepository.findByEmail(email);
        Purchase purchase = purchaseRepository.findOne(id);
        if ((user != null)&&(purchase != null)){
            return new ResponseEntity<>(HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
}