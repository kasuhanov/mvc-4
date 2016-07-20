package su.asgor.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import su.asgor.dao.CustomerRepository;
import su.asgor.dao.PurchaseRepository;
import su.asgor.model.Customer;
import su.asgor.model.Purchase;
import su.asgor.model.QCustomer;

@Controller
@RequestMapping("/customer")
public class CustomerController {
    @Autowired
    private CustomerRepository repository;
    @Autowired
    private PurchaseRepository purchaseRepository;

    @RequestMapping(value = "/{id}",method = RequestMethod.GET)
    @ResponseBody
    public Customer getCustomer(@PathVariable long id) {
        return repository.findOne(id);
    }

    @RequestMapping(value = "/{id}/purchases",method = RequestMethod.GET)
    @ResponseBody
    public Page<Purchase> getPurchasesPage(@PathVariable long id, @RequestParam(required = true) int page,
    		@RequestParam(required = true) int pageSize, @RequestParam String orderby, @RequestParam String order) {
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
        return purchaseRepository.findByCustomer(repository.findOne(id),pageable);
    }

    @RequestMapping(value = "/page",method = RequestMethod.GET)
    @ResponseBody
    public Page<Customer> getCustomersPage (@RequestParam int page,
                                              @RequestParam int size,
                                              @RequestParam String orderby,
                                              @RequestParam String order,
                                              @RequestParam String query){
        Pageable pageable;
        if(orderby == null || orderby.equals("undefined")){
            pageable= new PageRequest(page, size, Sort.Direction.DESC, "id");
        }else{
            if(order.equals("asc")){
                pageable= new PageRequest(page, size, Sort.Direction.ASC, orderby);
            }else{
                pageable= new PageRequest(page, size, Sort.Direction.DESC, orderby);
            }
        }
        if (query.equals(""))
            return repository.findAll(pageable);
        else{
            try {
                Long id = Long.valueOf(query);
                return repository.findAll(QCustomer.customer.name.containsIgnoreCase(query)
                        .or(QCustomer.customer.id.eq(id)),pageable);
            }catch (NumberFormatException e){
                return repository.findAll(QCustomer.customer.name.containsIgnoreCase(query),pageable);
            }
        }

    }
}