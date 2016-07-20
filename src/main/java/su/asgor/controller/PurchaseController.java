package su.asgor.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import su.asgor.dao.PurchaseRepository;
import su.asgor.dao.UserRepository;
import su.asgor.model.Purchase;
import su.asgor.model.User;
import su.asgor.service.PurchaseService;

import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/purchase")
public class PurchaseController {
    @Autowired
    private PurchaseRepository purchaseRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PurchaseService purchaseService;

    @RequestMapping(value = "/all",method = RequestMethod.GET)
    @ResponseBody
    public Iterable<Purchase> getAll() {
        return purchaseRepository.findAll();
    }

    @RequestMapping(value = "/page",method = RequestMethod.GET)
    @ResponseBody
    public Page<Purchase> getPageByCompleted (@RequestParam int page,
                                              @RequestParam int pageSize,
                                              @RequestParam String orderby,
                                              @RequestParam String order,
                                              Boolean completed){

        return purchaseService.getPurchaseByCompleted(page, pageSize, completed, orderby, order);
    }

    @RequestMapping(value = "/{id}",method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<?> getPurchase(@PathVariable String id) {
        Purchase result = purchaseRepository.findOne(id);
        if(result!= null)
            return new ResponseEntity<>(purchaseRepository.findOne(id).setupCompleted(), HttpStatus.OK);
        else
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @RequestMapping(value = "/for-user/{id}",method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<?> getPurchaseForUser(@PathVariable String id) {
        User user = userRepository.findByEmail(SecurityContextHolder.getContext().getAuthentication().getName());
        Purchase purchase = purchaseRepository.findOne(id);
        if (user == null)
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        if (purchase == null)
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        Map<String,Object> response = new HashMap<>();
        purchase.setupCompleted();
        response.put("fav", user.getFavs().contains(purchase));
        response.put("purchase", purchase);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @RequestMapping(value = "/for-user/page",method = RequestMethod.GET)
    @ResponseBody
    public Page<Purchase> getFavPage(@RequestParam int page, @RequestParam int pageSize,
                                     @RequestParam String orderby, @RequestParam String order){
        Pageable pageable;
        if(orderby == null || orderby.equals("undefined")){
            pageable= new PageRequest(page, pageSize, Sort.Direction.DESC, "publicationDate");
        }else{
            if(order.equals("asc")){
                pageable= new PageRequest(page, pageSize, Sort.Direction.ASC, orderby);
            }else{
                pageable= new PageRequest(page, pageSize, Sort.Direction.DESC, orderby);
            }
        }
        User user = userRepository.findByEmail(SecurityContextHolder.getContext().getAuthentication().getName());
        if(user==null)
            return null;
        return purchaseRepository.findByUsers_Email(user.getEmail(),pageable);
    }

    @RequestMapping(value = "/other/page",method = RequestMethod.GET)
    @ResponseBody
    public Page<Purchase> getOtherPage(@RequestParam int page, @RequestParam int pageSize,
                                       @RequestParam String orderby, @RequestParam String order){
        return purchaseService.getOther(page,pageSize,orderby,order);
    }
}