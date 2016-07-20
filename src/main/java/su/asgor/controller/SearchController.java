package su.asgor.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.querydsl.core.types.dsl.BooleanExpression;

import su.asgor.dao.PurchaseRepository;
import su.asgor.model.Purchase;
import su.asgor.model.PurchaseType;
import su.asgor.model.QPurchase;
import su.asgor.service.PurchaseService;

import java.util.Date;
import java.util.List;

@Controller
@RequestMapping("/search")
public class SearchController {
    @Autowired
    private PurchaseRepository purchaseRepository;

    @RequestMapping(value = "/quick",method = RequestMethod.GET)
    @ResponseBody
    public List<Purchase> quickSearch(@RequestParam(required = true)String text) {
        return purchaseRepository.findByNameContainingIgnoreCase(text);
    }

    @RequestMapping(value = "/quick_paged",method = RequestMethod.GET)
    @ResponseBody
    public Page<Purchase> quickPagedSearch(@RequestParam(required = true)String text,
                                           @RequestParam(required = true) int page, @RequestParam(required = true) int pageSize,
                                           @RequestParam String orderby, @RequestParam String order) {
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
        return purchaseRepository.findAll(QPurchase.purchase.name.containsIgnoreCase(text)
                .or(QPurchase.purchase.id.equalsIgnoreCase(text)),pageable);
    }

    @RequestMapping(value = "/advanced_paged",method = RequestMethod.GET)
    @ResponseBody
    public Page<Purchase> advancedPagedSearch(String id, String purchaseName, Long customer, Long startDate, Long endDate,
                Double minPrice, Double maxPrice, Long category, PurchaseType type, Boolean completed, String quick, String fz,
                @RequestParam(required = true) int page, @RequestParam(required = true) int pageSize,
                                              @RequestParam String orderby, @RequestParam String order) {
        Pageable pageable;
        if(orderby == null || orderby.equals("undefined")){
            pageable= new PageRequest(page, pageSize, Sort.Direction.DESC, "publicationDate");
        }else{
            if(order.equals("asc")){
                pageable= new PageRequest(page, pageSize, Sort.Direction.ASC, orderby);
            }else{
                pageable= new PageRequest(page, pageSize, Sort.Direction.DESC, orderby);
            }
        };
        QPurchase qPurchase = QPurchase.purchase;
        BooleanExpression predicate = qPurchase.isNotNull();
        if(quick!=null){
            predicate = predicate.and(qPurchase.name.containsIgnoreCase(quick)
                    .or(qPurchase.id.equalsIgnoreCase(quick)));
        }
        if(id!=null){
            predicate = predicate.and(qPurchase.id.eq(id));
        }
        if(purchaseName!=null){
            predicate = predicate.and(qPurchase.name.containsIgnoreCase(purchaseName));
        }
        if(customer!=null){
            predicate = predicate.and(qPurchase.customer.id.eq(customer));
        }
        if(category!=null){
            if(category==-1)
                predicate = predicate.and(qPurchase.categories.isEmpty());
            else
                predicate = predicate.and(qPurchase.categories.any().id.eq(category));
        }
        if(minPrice!=null){
            predicate = predicate.and(qPurchase.startPrice.goe(minPrice));
        }
        if(maxPrice!=null){
            predicate = predicate.and(qPurchase.startPrice.loe(maxPrice));
        }
        if(startDate!=null){
            Date date = new Date(startDate);
            predicate = predicate.and(qPurchase.submissionCloseDate.goe(date));
        }
        if(endDate!=null){
            Date date = new Date(endDate);
            predicate = predicate.and(qPurchase.submissionCloseDate.loe(date));
        }
        if(type!=null){
            predicate = predicate.and(qPurchase.type.eq(type));
        }
        if(fz!=null){
            predicate = predicate.and(qPurchase.fz.eq(fz));
        }
        if(completed!=null){
            if (completed){
                predicate = predicate.and(PurchaseService.completedExpression());
            } else {
                predicate = predicate.and(PurchaseService.uncompletedExpression());
            }
        }
        return purchaseRepository.findAll(predicate,pageable);
    }
}