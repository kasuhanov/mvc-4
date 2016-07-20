package su.asgor.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import su.asgor.dao.CategoryRepository;
import su.asgor.dao.UserRepository;
import su.asgor.model.Category;
import su.asgor.model.User;

import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/subscribe")
public class SubscriptionController {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private CategoryRepository categoryRepository;

    @RequestMapping(value = "/",method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity<?> subscribe(@RequestBody Map<Long,Boolean> request){
        User user = userRepository.findByEmail(SecurityContextHolder.getContext().getAuthentication().getName());
        if(user != null){
            List<Category> categories = user.getSubscriptions();
            for(Map.Entry<Long,Boolean> entry:request.entrySet()){
                Category category = categoryRepository.findOne(entry.getKey());
                if (!categories.contains(category) && entry.getValue()){
                    categories.add(categoryRepository.findOne(entry.getKey()));
                }
                if (categories.contains(category) && !entry.getValue()) {
                    categories.remove(category);
                }
                userRepository.save(user);
            }
            return new ResponseEntity<>(HttpStatus.OK);
        }else return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
    }

    @RequestMapping(value = "/sub/",method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<?> subscribe(@RequestParam Long id,@RequestParam Boolean sel){
        User user = userRepository.findByEmail(SecurityContextHolder.getContext().getAuthentication().getName());
        if(user != null){
            List<Category> categories = user.getSubscriptions();
            Category category = categoryRepository.findOne(id);
                if (!categories.contains(category) && sel){
                    categories.add(category);
                }
                if (categories.contains(category) && !sel) {
                    categories.remove(category);
                }
                userRepository.save(user);
            return new ResponseEntity<>(HttpStatus.OK);
        }else return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
    }

    @RequestMapping(value = "/",method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<?> getSubscriptions(){
        User user = userRepository.findByEmail(SecurityContextHolder.getContext().getAuthentication().getName());
        if (user == null)
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        List<Category> categories = categoryRepository.findAll();
        for(Category category:user.getSubscriptions()){
            if(categories.contains(category)){
                categories.remove(category);
                category.setSubscribed(true);
                categories.add(category);
            }
        }
        return new ResponseEntity<>(categories, HttpStatus.OK);
    }

    @RequestMapping(value = "/notify-favs",method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<?> getNotifyFavsChange(){
        User user = userRepository.findByEmail(SecurityContextHolder.getContext().getAuthentication().getName());
        if (user == null)
           return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        return  new ResponseEntity<>(user.getNotifyFavsChange(),HttpStatus.OK);
    }

    @RequestMapping(value = "/notify-favs",method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity<?> setNotifyFavsChange(@RequestBody Boolean notify){
        User user = userRepository.findByEmail(SecurityContextHolder.getContext().getAuthentication().getName());
        if (user == null)
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        user.setNotifyFavsChange(notify);
        userRepository.save(user);
        return  new ResponseEntity<>(HttpStatus.OK);
    }
}
