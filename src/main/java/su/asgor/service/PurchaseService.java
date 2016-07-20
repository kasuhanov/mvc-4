package su.asgor.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.querydsl.core.types.dsl.BooleanExpression;

import su.asgor.dao.PurchaseRepository;
import su.asgor.model.Purchase;
import su.asgor.model.PurchaseType;
import su.asgor.model.QPurchase;

import java.util.Date;

@Service
public class PurchaseService {
    @Autowired
    private PurchaseRepository purchaseRepository;

    public Page<Purchase> getPurchaseByCompleted(int page, int size, Boolean completed, String orderby, String order){
        Pageable pageable;
        if(orderby == null || orderby.equals("undefined")){
            pageable= new PageRequest(page, size, Sort.Direction.DESC, "publicationDate");
        }else{
            if(order.equals("asc")){
                pageable= new PageRequest(page, size, Sort.Direction.ASC, orderby);
            }else{
                pageable= new PageRequest(page, size, Sort.Direction.DESC, orderby);
            }
        }
        if(completed == null){
            return purchaseRepository.findAll(pageable);
        }
        if (completed){
            return getCompletedPurchase(pageable);
        } else {
            return getUncompletedPurchase(pageable);
        }
    }

    public Page<Purchase> getOther(int page, int size, String orderby, String order){
        Pageable pageable;
        if(orderby == null || orderby.equals("undefined")){
            pageable= new PageRequest(page, size, Sort.Direction.DESC, "publicationDate");
        }else{
            if(order.equals("asc")){
                pageable= new PageRequest(page, size, Sort.Direction.ASC, orderby);
            }else{
                pageable= new PageRequest(page, size, Sort.Direction.DESC, orderby);
            }
        }
        return purchaseRepository.findAll(QPurchase.purchase.categories.isEmpty(), pageable);
    }

    public static boolean isCompleted(Purchase purchase){
        if((purchase.getType().equals(PurchaseType.AE) && purchase.getAuctionTime().after(new Date())) ||
                (purchase.getType().equals(PurchaseType.OA) && purchase.getAuctionTime().after(new Date())) ||
                (purchase.getType().equals(PurchaseType.OK) && purchase.getSummingupTime().after(new Date())) ||
                (purchase.getType().equals(PurchaseType.ZK) && purchase.getQuotationExaminationTime().after(new Date())) ||
                (purchase.getType().equals(PurchaseType.OKOU) && purchase.getSummingupTime().after(new Date())) ||
                (purchase.getType().equals(PurchaseType.ZA) && purchase.getAuctionTime().after(new Date())) ||
                (purchase.getType().equals(PurchaseType.ZP) && purchase.getQuotationExaminationTime().after(new Date())))
            return false;
        else
            return true;
    }

    public static BooleanExpression uncompletedExpression(){
        QPurchase qPurchase = QPurchase.purchase;
        BooleanExpression okou = qPurchase.type.eq(PurchaseType.OKOU).and(qPurchase.summingupTime.after(new Date()));
        BooleanExpression za = qPurchase.type.eq(PurchaseType.ZA).and(qPurchase.auctionTime.after(new Date()));
        BooleanExpression zp = qPurchase.type.eq(PurchaseType.ZP).and(qPurchase.quotationExaminationTime.after(new Date()));
        BooleanExpression ae = qPurchase.type.eq(PurchaseType.AE).and(qPurchase.auctionTime.after(new Date()));
        BooleanExpression oa = qPurchase.type.eq(PurchaseType.OA).and(qPurchase.auctionTime.after(new Date()));
        BooleanExpression ok = qPurchase.type.eq(PurchaseType.OK).and(qPurchase.summingupTime.after(new Date()));
        BooleanExpression zk = qPurchase.type.eq(PurchaseType.ZK).and(qPurchase.quotationExaminationTime.after(new Date()));
        return ae.or(oa).or(ok).or(zk).or(okou).or(za).or(zp);
    }

    public static BooleanExpression completedExpression(){
        QPurchase qPurchase = QPurchase.purchase;
        BooleanExpression okou = qPurchase.type.eq(PurchaseType.OKOU).and(qPurchase.summingupTime.before(new Date()));
        BooleanExpression za = qPurchase.type.eq(PurchaseType.ZA).and(qPurchase.auctionTime.before(new Date()));
        BooleanExpression zp = qPurchase.type.eq(PurchaseType.ZP).and(qPurchase.quotationExaminationTime.before(new Date()));
        BooleanExpression ae = qPurchase.type.eq(PurchaseType.AE).and(qPurchase.auctionTime.before(new Date()));
        BooleanExpression oa = qPurchase.type.eq(PurchaseType.OA).and(qPurchase.auctionTime.before(new Date()));
        BooleanExpression ok = qPurchase.type.eq(PurchaseType.OK).and(qPurchase.summingupTime.before(new Date()));
        BooleanExpression zk = qPurchase.type.eq(PurchaseType.ZK).and(qPurchase.quotationExaminationTime.before(new Date()));
        BooleanExpression ep = qPurchase.type.eq(PurchaseType.EP);
        return ae.or(oa).or(ok).or(zk).or(ep).or(okou).or(za).or(zp);
    }

    public Page<Purchase> getUncompletedPurchase(Pageable pageable){
        return purchaseRepository.findAll(uncompletedExpression(),pageable);
    }

    public Page<Purchase> getCompletedPurchase(Pageable pageable){
        return purchaseRepository.findAll(completedExpression(),pageable);
    }
}
