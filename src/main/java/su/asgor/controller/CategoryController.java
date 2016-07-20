package su.asgor.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import su.asgor.dao.CategoryRepository;
import su.asgor.dao.PurchaseRepository;
import su.asgor.model.Category;
import su.asgor.model.Purchase;
import su.asgor.model.QPurchase;
import su.asgor.service.PurchaseService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Controller
@RequestMapping("/category")
public class CategoryController {
    @Autowired
    private CategoryRepository repository;
    @Autowired
    private PurchaseRepository purchaseRepository;

    @RequestMapping(value = "/all",method = RequestMethod.GET)
    @ResponseBody
    public Iterable<Category> getAll() {
        return repository.findAll();
    }
    
    @RequestMapping(value = "/qwe",method = RequestMethod.GET)
    @ResponseBody
    public Iterable<Category> get() {
    	List<Category> cc = new ArrayList<Category> ();
    	Category c =  new Category();
    	c.setId(1);
    	c.setName("qwe");
    	cc.add(c);
        return cc;
    }

    @RequestMapping(value = "/{id}/purchases",method = RequestMethod.GET)
    @ResponseBody
    public Page<Purchase> getPurchasesPage(@PathVariable long id, @RequestParam int page,
    		@RequestParam int pageSize, @RequestParam String orderby, @RequestParam String order) {
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
        return purchaseRepository.findByCategories(repository.findOne(id),pageable);
    }

    @RequestMapping(value = "/{id}",method = RequestMethod.GET)
    @ResponseBody
    public Category getByID(@PathVariable long id) {
        return repository.findOne(id);
    }

    @RequestMapping(value = "purchase/{id}",method = RequestMethod.GET)
    @ResponseBody
    @Transactional
    public ResponseEntity<?> getByPurchase(@PathVariable String id) {
        Purchase purchase = purchaseRepository.findOne(id);
        if (purchase!=null){
        	purchase.getCategories().size();
            return new ResponseEntity<>(purchase.getCategories(),HttpStatus.OK);
        }        	
        else
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @RequestMapping(value = "/",method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity<?> addCategory(@RequestBody String name) {
        if(repository.findByName(name)==null) {
            Category category = new Category(name);
            repository.save(category);
            return new ResponseEntity<>(category,HttpStatus.OK);
        }else {
            return new ResponseEntity<>(HttpStatus.CONFLICT);
        }
    }

    @RequestMapping(value = "/{id}",method = RequestMethod.DELETE)
    @ResponseBody
    public ResponseEntity<?> deleteCategory(@PathVariable long id) {
        repository.delete(id);
        if(repository.findOne(id)==null) {
            return new ResponseEntity<>(HttpStatus.OK);
        }else {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @RequestMapping(value = "/basic-count",method = RequestMethod.GET)
    @ResponseBody
    public Map<String,Long> getCountForBasicCategories() {
        Map<String,Long> result = new HashMap<>();
        result.put("all",purchaseRepository.count());
        result.put("uncompleted",purchaseRepository.count(PurchaseService.uncompletedExpression()));
        result.put("other",purchaseRepository.count(QPurchase.purchase.categories.isEmpty()));
        return result;
    }
}
