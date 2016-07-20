package su.asgor.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import su.asgor.dao.CustomerRepository;
import su.asgor.dao.PurchaseRepository;
import su.asgor.model.*;
import su.asgor.parser.generated.fz223.PurchaseNoticeStatusType;
import su.asgor.parser.generated.fz44.*;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

@Service
@Transactional
public class ParserFz44Service {
	@Autowired
	private PurchaseRepository purchaseRepository;
	@Autowired
	private CustomerRepository customerRepository;
    @Autowired
    private CategoryService categoryService;
	private final Logger log = LoggerFactory.getLogger(this.getClass());

    public Purchase parse(XMLFile xmlFile) throws Exception {
        String pathToFile = xmlFile.getPath();
        String fileName = xmlFile.getName();
        boolean correctFileName = false;
        Purchase purchase = null;
        try {
        	if(fileName.startsWith("fcsNotificationEA")){
                correctFileName = true;
                purchase = parseEA(pathToFile,fileName);
            }
            if(fileName.startsWith("fcsNotificationEP")){
                correctFileName = true;
                purchase = parseEP(pathToFile,fileName);
            }
            if(fileName.startsWith("fcsNotificationOKD")){
                correctFileName = true;
                purchase = parseOKD(pathToFile,fileName);
            }
            if(fileName.startsWith("fcsNotificationOKOU")){
                correctFileName = true;
                purchase = parseOKOU(pathToFile,fileName);
            }
            if(fileName.startsWith("fcsNotificationOK")){
                correctFileName = true;
                purchase = parseOK(pathToFile,fileName);
            }
            if(fileName.startsWith("fcsNotificationZA")){
                correctFileName = true;
                purchase = parseZA(pathToFile,fileName);
            }
            if(fileName.startsWith("fcsNotificationZK")){
                correctFileName = true;
                purchase = parseZK(pathToFile,fileName);
            }
            if(fileName.startsWith("fcsNotificationZP")){
                correctFileName = true;
                purchase = parseZP(pathToFile,fileName);
            }
            if(!correctFileName){
                throw new Exception("unknown fz44 filename");
            }
            log.info("P type, purchase №"+purchase.getId());
            if(purchaseRepository.exists(purchase.getId())){
                log.info("updating existing purchase");
                if(purchase.getEventDate().after(purchaseRepository.findOne(purchase.getId()).getEventDate())){
                    purchase.setStatus(PurchaseNoticeStatusType.M);
                    purchase.setAfter(true);
                }else{
                    purchase.setEventDate(purchaseRepository.findOne(purchase.getId()).getEventDate());
                    purchase.setAfter(false);
                }
            }else {
                purchase.setStatus(PurchaseNoticeStatusType.P);
                purchase.setAfter(true);
            }
            customerRepository.save(purchase.getCustomer());
            purchase.setFileName(fileName);
            purchaseRepository.save(categoryService.setupCategory(purchase));
            return purchase;
		} catch (Exception e) {
			log.error("exception in file:"+fileName+" Messaage:"+e.getMessage(),e);
			throw new Exception(e);
		}
    }

    private Purchase parseEA(String pathToFile, String fileName) throws JAXBException {
        log.info("Аукцион в электронном виде - "+fileName);
        JAXBContext jc = JAXBContext.newInstance(Export.class);
        Unmarshaller unmarshaller = jc.createUnmarshaller();
        ZfcsNotificationEFType data = ((Export)unmarshaller.unmarshal(new File(pathToFile))).getFcsNotificationEF();
        Customer customer = parseCustomerEA(data.getPurchaseResponsible());
        Purchase purchase = parseCommonPurchasePropsEA(data);
        purchase.setType(PurchaseType.AE);
        purchase.setSubmissionCloseDate(data.getProcedureInfo().getCollecting().getEndDate()
                .toGregorianCalendar().getTime());
        purchase.setAuctionTime(data.getProcedureInfo().getBidding().getDate().toGregorianCalendar().getTime());
        purchase.setDocumentationDeliveryPlace(data.getProcedureInfo().getCollecting().getPlace());
        purchase.setDocumentationDeliveryProcedure(data.getProcedureInfo().getCollecting().getOrder());
        if(purchase.getAuctionTime().before(new Date()))
            purchase.setCompleted(true);
        purchase.setElectronicPlaceName(data.getETP().getName());
        purchase.setElectronicPlaceUrl(
                data.getETP().getUrl().startsWith("http") ?
                        data.getETP().getUrl() : "http://" + data.getETP().getUrl()
        );
        List<Preferense> preferenses = new ArrayList<>();
        if(data.getLot().getPreferenses() != null)
            for (ZfcsPreferenseType pref : data.getLot().getPreferenses().getPreferense()){
                Preferense preferense = new Preferense();
                preferense.setId(pref.getCode());
                preferense.setName(pref.getName());
                preferense.setValue(pref.getPrefValue());
                preferense.setPurchase(purchase);
                preferenses.add(preferense);
            }
        purchase.setPreferenses(preferenses);
        purchase.setStartPrice(Double.valueOf(data.getLot().getPurchaseObjects().getTotalSum()));
        purchase.setApplicationGuarantee(Double.valueOf(data.getLot().getCustomerRequirements()
                .getCustomerRequirement().get(0).getApplicationGuarantee().getAmount()));
        if(data.getLot().getCustomerRequirements().getCustomerRequirement().get(0).getContractGuarantee()!= null)
            purchase.setContractGuarantee(Double.valueOf(data.getLot().getCustomerRequirements()
                    .getCustomerRequirement().get(0).getContractGuarantee().getAmount()));
        Lot lot = new Lot();
        lot.setId(data.getPurchaseNumber());
        lot.setPurchase(purchase);
        lot.setName(data.getPurchaseObjectInfo());
        lot.setAddress(data.getLot().getCustomerRequirements().getCustomerRequirement().get(0)
                .getKladrPlaces().getKladrPlace().get(0).getDeliveryPlace());
        lot.setStartPrice(Double.valueOf(data.getLot().getPurchaseObjects().getTotalSum()));
        lot.setCurrency(data.getLot().getCurrency().getCode());
        List<LotItem> lotItems = new ArrayList<>();
        int i = 0;
        for (ZfcsNotificationEFType.Lot.PurchaseObjects.PurchaseObject purchaseObject
                    : data.getLot().getPurchaseObjects().getPurchaseObject()){
            LotItem lotItem = new LotItem();
            lotItem.setLot(lot);
            lotItem.setId(lot.getId()+(i++));
            if(purchaseObject.getName()!=null)
                lotItem.setName(purchaseObject.getName());
            try {
                lotItem.setCodeOkpd2(purchaseObject.getOKPD().getCode());
            }catch (Exception e){
                lotItem.setCodeOkpd2(purchaseObject.getOKPD2().getCode());
            }
            if(purchaseObject.getOKEI() != null)
                lotItem.setOkeiName(purchaseObject.getOKEI().getNationalCode());
            lotItem.setQty(purchaseObject.getQuantity().getValue());
            if(purchaseObject.getSum()!=null)
                lotItem.setSum(Double.valueOf(purchaseObject.getSum()));
            if(purchaseObject.getPrice() != null)
                lotItem.setPrice(Double.valueOf(purchaseObject.getPrice()));
            lotItems.add(lotItem);
        }
        lot.setLotItems(lotItems);
        purchase.setLots(Collections.singletonList(lot));
        purchase.setDocument(parseDocs(data.getAttachments(), purchase));
        purchase.setCustomer(customer);
        return purchase;
    }

    private Purchase parseOK(String pathToFile, String fileName) throws JAXBException {
        log.info("Открытый конкурс - "+fileName);
        JAXBContext jc = JAXBContext.newInstance(Export.class);
        Unmarshaller unmarshaller = jc.createUnmarshaller();
        ZfcsNotificationOKType data = ((Export)unmarshaller.unmarshal(new File(pathToFile))).getFcsNotificationOK();
        Customer customer = parseCustomerEA(data.getPurchaseResponsible());
        Purchase purchase = parseCommonPurchasePropsEA(data);
        purchase.setType(PurchaseType.OK);
        purchase.setSummingupPlace(data.getProcedureInfo().getOpening().getPlace());
        purchase.setSummingupTime(data.getProcedureInfo().getOpening().getDate().toGregorianCalendar().getTime());
        purchase.setEnvelopeOpeningPlace(data.getProcedureInfo().getOpening().getPlace());
        purchase.setEnvelopeOpeningTime(data.getProcedureInfo().getOpening().getDate().toGregorianCalendar().getTime());
        purchase.setExaminationPlace(data.getProcedureInfo().getScoring().getPlace());
        purchase.setExaminationDateTime(data.getProcedureInfo().getScoring().getDate().toGregorianCalendar().getTime());
        purchase.setSubmissionCloseDate(data.getProcedureInfo().getCollecting().getEndDate()
                .toGregorianCalendar().getTime());
        purchase.setSubmissionOrder(data.getProcedureInfo().getCollecting().getOrder());
        purchase.setSubmissionPlace(data.getProcedureInfo().getCollecting().getPlace());
        purchase.setDocumentationDeliveryPlace(data.getProcedureInfo().getCollecting().getPlace());
        purchase.setDocumentationDeliveryProcedure(data.getProcedureInfo().getCollecting().getOrder());
        List<Preferense> preferenses = new ArrayList<>();
        for (ZfcsNotificationOKType.Lots.Lot lt: data.getLots().getLot()){
            if(lt.getPreferenses() != null)
                for (ZfcsPreferenseType pref : lt.getPreferenses().getPreferense()){
                    Preferense preferense = new Preferense();
                    preferense.setId(pref.getCode());
                    preferense.setName(pref.getName());
                    preferense.setValue(pref.getPrefValue());
                    preferense.setPurchase(purchase);
                    preferenses.add(preferense);
                }
            purchase.setPreferenses(preferenses);
        }
        purchase.setStartPrice(0);
        purchase.setApplicationGuarantee(Double.valueOf(data.getLots().getLot().get(0).getCustomerRequirements()
                .getCustomerRequirement().get(0).getApplicationGuarantee().getAmount()));
        purchase.setContractGuarantee(Double.valueOf(data.getLots().getLot().get(0).getCustomerRequirements()
                .getCustomerRequirement().get(0).getContractGuarantee().getAmount()));
        List<Lot> lots = new ArrayList<>();
        for (ZfcsNotificationOKType.Lots.Lot lt: data.getLots().getLot()){
            Lot lot = new Lot();
            lot.setId(data.getPurchaseNumber());
            lot.setPurchase(purchase);
            lot.setName(data.getPurchaseObjectInfo());
            lot.setAddress(lt.getCustomerRequirements().getCustomerRequirement().get(0)
                    .getKladrPlaces().getKladrPlace().get(0).getDeliveryPlace());
            lot.setStartPrice(Double.valueOf(lt.getPurchaseObjects().getTotalSum()));
            lot.setCurrency(lt.getCurrency().getCode());
            List<LotItem> lotItems = new ArrayList<>();
            int i = 0;
            for (ZfcsNotificationOKType.Lots.Lot.PurchaseObjects.PurchaseObject purchaseObject
                    : lt.getPurchaseObjects().getPurchaseObject()){
                LotItem lotItem = new LotItem();
                lotItem.setLot(lot);
                lotItem.setId(lot.getId()+(i++));
                if(purchaseObject.getName()!=null)
                    lotItem.setName(purchaseObject.getName());
                try {
                    lotItem.setCodeOkpd2(purchaseObject.getOKPD().getCode());
                }catch (Exception e){
                    lotItem.setCodeOkpd2(purchaseObject.getOKPD2().getCode());
                }
                if(purchaseObject.getOKEI() != null)
                    lotItem.setOkeiName(purchaseObject.getOKEI().getNationalCode());
                lotItem.setQty(purchaseObject.getQuantity().getValue());
                lotItem.setSum(Double.valueOf(purchaseObject.getSum()));
                if(purchaseObject.getPrice() != null)
                    lotItem.setPrice(Double.valueOf(purchaseObject.getPrice()));
                lotItems.add(lotItem);
            }
            lot.setLotItems(lotItems);
            lots.add(lot);
            purchase.setStartPrice(purchase.getStartPrice()+Double.valueOf(lt.getPurchaseObjects().getTotalSum()));
        }
        purchase.setLots(lots);
        purchase.setDocument(parseDocs(data.getAttachments(), purchase));
        purchase.setCustomer(customer);
        return purchase;
    }

    private Purchase parseOKOU(String pathToFile, String fileName) throws JAXBException {
        log.info("Конкурс с ограниченным участием - "+fileName);
        JAXBContext jc = JAXBContext.newInstance(Export.class);
        Unmarshaller unmarshaller = jc.createUnmarshaller();
        ZfcsNotificationOKOUType data = ((Export)unmarshaller.unmarshal(new File(pathToFile))).getFcsNotificationOKOU();
        Customer customer = parseCustomerEA(data.getPurchaseResponsible());
        Purchase purchase = parseCommonPurchasePropsEA(data);
        purchase.setType(PurchaseType.OKOU);
        purchase.setSummingupPlace(data.getProcedureInfo().getOpening().getPlace());
        purchase.setSummingupTime(data.getProcedureInfo().getOpening().getDate().toGregorianCalendar().getTime());
        purchase.setEnvelopeOpeningPlace(data.getProcedureInfo().getOpening().getPlace());
        purchase.setEnvelopeOpeningTime(data.getProcedureInfo().getOpening().getDate().toGregorianCalendar().getTime());
        purchase.setExaminationPlace(data.getProcedureInfo().getScoring().getPlace());
        purchase.setExaminationDateTime(data.getProcedureInfo().getScoring().getDate().toGregorianCalendar().getTime());
        purchase.setSubmissionCloseDate(data.getProcedureInfo().getCollecting().getEndDate()
                .toGregorianCalendar().getTime());
        purchase.setSubmissionOrder(data.getProcedureInfo().getCollecting().getOrder());
        purchase.setSubmissionPlace(data.getProcedureInfo().getCollecting().getPlace());
        purchase.setDocumentationDeliveryPlace(data.getProcedureInfo().getCollecting().getPlace());
        purchase.setDocumentationDeliveryProcedure(data.getProcedureInfo().getCollecting().getOrder());
        List<Preferense> preferenses = new ArrayList<>();
        for (ZfcsNotificationOKOUType.Lots.Lot lt: data.getLots().getLot()){
            if(lt.getPreferenses() != null)
                for (ZfcsPreferenseType pref : lt.getPreferenses().getPreferense()){
                    Preferense preferense = new Preferense();
                    preferense.setId(pref.getCode());
                    preferense.setName(pref.getName());
                    preferense.setValue(pref.getPrefValue());
                    preferense.setPurchase(purchase);
                    preferenses.add(preferense);
                }
            purchase.setPreferenses(preferenses);
        }
        purchase.setStartPrice(0);
        purchase.setApplicationGuarantee(Double.valueOf(data.getLots().getLot().get(0).getCustomerRequirements()
                .getCustomerRequirement().get(0).getApplicationGuarantee().getAmount()));
        purchase.setContractGuarantee(Double.valueOf(data.getLots().getLot().get(0).getCustomerRequirements()
                .getCustomerRequirement().get(0).getContractGuarantee().getAmount()));
        List<Lot> lots = new ArrayList<>();
        for (ZfcsNotificationOKOUType.Lots.Lot lt: data.getLots().getLot()){
            Lot lot = new Lot();
            lot.setId(data.getPurchaseNumber());
            lot.setPurchase(purchase);
            lot.setName(data.getPurchaseObjectInfo());
            lot.setAddress(lt.getCustomerRequirements().getCustomerRequirement().get(0)
                    .getKladrPlaces().getKladrPlace().get(0).getDeliveryPlace());
            lot.setStartPrice(Double.valueOf(lt.getPurchaseObjects().getTotalSum()));
            lot.setCurrency(lt.getCurrency().getCode());
            List<LotItem> lotItems = new ArrayList<>();
            int i = 0;
            for (ZfcsNotificationOKType.Lots.Lot.PurchaseObjects.PurchaseObject purchaseObject
                    : lt.getPurchaseObjects().getPurchaseObject()){
                LotItem lotItem = new LotItem();
                lotItem.setLot(lot);
                lotItem.setId(lot.getId()+(i++));
                if(purchaseObject.getName()!=null)
                    lotItem.setName(purchaseObject.getName());
                try {
                    lotItem.setCodeOkpd2(purchaseObject.getOKPD().getCode());
                }catch (Exception e){
                    lotItem.setCodeOkpd2(purchaseObject.getOKPD2().getCode());
                }
                if(purchaseObject.getOKEI() != null)
                    lotItem.setOkeiName(purchaseObject.getOKEI().getNationalCode());
                lotItem.setQty(purchaseObject.getQuantity().getValue());
                lotItem.setSum(Double.valueOf(purchaseObject.getSum()));
                if(purchaseObject.getPrice() != null)
                    lotItem.setPrice(Double.valueOf(purchaseObject.getPrice()));
                lotItems.add(lotItem);
            }
            lot.setLotItems(lotItems);
            lots.add(lot);
            purchase.setStartPrice(purchase.getStartPrice()+Double.valueOf(lt.getPurchaseObjects().getTotalSum()));
        }
        purchase.setLots(lots);
        purchase.setDocument(parseDocs(data.getAttachments(), purchase));
        purchase.setCustomer(customer);
        return purchase;
    }

    private Purchase parseOKD(String pathToFile, String fileName) throws JAXBException {
        log.info("Двухэтапный конкурс - "+fileName);
        JAXBContext jc = JAXBContext.newInstance(Export.class);
        Unmarshaller unmarshaller = jc.createUnmarshaller();
        ZfcsNotificationOKDType data = ((Export)unmarshaller.unmarshal(new File(pathToFile))).getFcsNotificationOKD();
        Customer customer = parseCustomerEA(data.getPurchaseResponsible());
        Purchase purchase = parseCommonPurchasePropsEA(data);
        purchase.setType(PurchaseType.OKD);
        List<Preferense> preferenses = new ArrayList<>();
        for (ZfcsNotificationOKDType.Lots.Lot lt: data.getLots().getLot()){
            if(lt.getPreferenses() != null)
                for (ZfcsPreferenseType pref : lt.getPreferenses().getPreferense()){
                    Preferense preferense = new Preferense();
                    preferense.setId(pref.getCode());
                    preferense.setName(pref.getName());
                    preferense.setValue(pref.getPrefValue());
                    preferense.setPurchase(purchase);
                    preferenses.add(preferense);
                }
            purchase.setPreferenses(preferenses);
        }
        purchase.setStartPrice(0);
        purchase.setApplicationGuarantee(Double.valueOf(data.getLots().getLot().get(0).getCustomerRequirements()
                .getCustomerRequirement().get(0).getApplicationGuarantee().getAmount()));
        purchase.setContractGuarantee(Double.valueOf(data.getLots().getLot().get(0).getCustomerRequirements()
                .getCustomerRequirement().get(0).getContractGuarantee().getAmount()));
        List<Lot> lots = new ArrayList<>();
        for (ZfcsNotificationOKDType.Lots.Lot lt: data.getLots().getLot()){
            Lot lot = new Lot();
            lot.setId(data.getPurchaseNumber());
            lot.setPurchase(purchase);
            lot.setName(data.getPurchaseObjectInfo());
            lot.setAddress(lt.getCustomerRequirements().getCustomerRequirement().get(0)
                    .getKladrPlaces().getKladrPlace().get(0).getDeliveryPlace());
            lot.setStartPrice(Double.valueOf(lt.getPurchaseObjects().getTotalSum()));
            lot.setCurrency(lt.getCurrency().getCode());
            List<LotItem> lotItems = new ArrayList<>();
            int i = 0;
            for (ZfcsNotificationOKType.Lots.Lot.PurchaseObjects.PurchaseObject purchaseObject
                    : lt.getPurchaseObjects().getPurchaseObject()){
                LotItem lotItem = new LotItem();
                lotItem.setLot(lot);
                lotItem.setId(lot.getId()+(i++));
                if(purchaseObject.getName()!=null)
                    lotItem.setName(purchaseObject.getName());
                try {
                    lotItem.setCodeOkpd2(purchaseObject.getOKPD().getCode());
                }catch (Exception e){
                    lotItem.setCodeOkpd2(purchaseObject.getOKPD2().getCode());
                }
                if(purchaseObject.getOKEI() != null)
                    lotItem.setOkeiName(purchaseObject.getOKEI().getNationalCode());
                lotItem.setQty(purchaseObject.getQuantity().getValue());
                lotItem.setSum(Double.valueOf(purchaseObject.getSum()));
                if(purchaseObject.getPrice() != null)
                    lotItem.setPrice(Double.valueOf(purchaseObject.getPrice()));
                lotItems.add(lotItem);
            }
            lot.setLotItems(lotItems);
            lots.add(lot);
            purchase.setStartPrice(purchase.getStartPrice()+Double.valueOf(lt.getPurchaseObjects().getTotalSum()));
        }
        purchase.setLots(lots);
        purchase.setDocument(parseDocs(data.getAttachments(), purchase));
        purchase.setCustomer(customer);
        return purchase;
    }

    private Purchase parseEP(String pathToFile, String fileName) throws JAXBException {
        log.info("Закупка у единственного поставщика - "+fileName);
        JAXBContext jc = JAXBContext.newInstance(Export.class);
        Unmarshaller unmarshaller = jc.createUnmarshaller();
        ZfcsNotificationEPType data = ((Export)unmarshaller.unmarshal(new File(pathToFile))).getFcsNotificationEP();
        Customer customer = parseCustomerEP(data.getPurchaseResponsible());
        Purchase purchase = parseCommonPurchasePropsEP(data);
        purchase.setType(PurchaseType.EP);
        purchase.setCompleted(true);
        List<Preferense> preferenses = new ArrayList<>();
        if(data.getLot().getPreferenses() != null)
            for (ZfcsPreferenseType pref : data.getLot().getPreferenses().getPreferense()){
                Preferense preferense = new Preferense();
                preferense.setId(pref.getCode());
                preferense.setName(pref.getName());
                preferense.setValue(pref.getPrefValue());
                preferense.setPurchase(purchase);
                preferenses.add(preferense);
            }
        purchase.setPreferenses(preferenses);
        purchase.setStartPrice(Double.valueOf(data.getLot().getPurchaseObjects().getTotalSum()));
        Lot lot = new Lot();
        lot.setId(data.getPurchaseNumber());
        lot.setPurchase(purchase);
        lot.setName(data.getPurchaseObjectInfo());
        lot.setAddress(data.getLot().getCustomerRequirements().getCustomerRequirement().get(0)
                .getKladrPlaces().getKladrPlace().get(0).getDeliveryPlace());
        lot.setStartPrice(Double.valueOf(data.getLot().getPurchaseObjects().getTotalSum()));
        lot.setCurrency(data.getLot().getCurrency().getCode());
        List<LotItem> lotItems = new ArrayList<>();
        int i = 0;
        for (ZfcsNotificationEPType.Lot.PurchaseObjects.PurchaseObject purchaseObject
                : data.getLot().getPurchaseObjects().getPurchaseObject()){
            LotItem lotItem = new LotItem();
            lotItem.setLot(lot);
            lotItem.setId(lot.getId()+(i++));
            if(purchaseObject.getName()!=null)
                lotItem.setName(purchaseObject.getName());
            try {
                lotItem.setCodeOkpd2(purchaseObject.getOKPD().getCode());
            }catch (Exception e){
                lotItem.setCodeOkpd2(purchaseObject.getOKPD2().getCode());
            }
            if(purchaseObject.getOKEI() != null)
                lotItem.setOkeiName(purchaseObject.getOKEI().getNationalCode());
            lotItem.setQty(purchaseObject.getQuantity().getValue());
            lotItem.setSum(Double.valueOf(purchaseObject.getSum()));
            if(purchaseObject.getPrice() != null)
                lotItem.setPrice(Double.valueOf(purchaseObject.getPrice()));
            lotItems.add(lotItem);
        }
        lot.setLotItems(lotItems);
        purchase.setLots(Collections.singletonList(lot));
        purchase.setDocument(parseDocs(data.getAttachments(), purchase));
        purchase.setCustomer(customer);
        return purchase;
    }

    private Purchase parseZA(String pathToFile, String fileName) throws JAXBException {
        log.info("Закрытый аукцион - "+fileName);
        JAXBContext jc = JAXBContext.newInstance(Export.class);
        Unmarshaller unmarshaller = jc.createUnmarshaller();
        ZfcsNotificationZakAType data = ((Export)unmarshaller.unmarshal(new File(pathToFile))).getFcsNotificationZakA();
        Customer customer = parseCustomerEA(data.getPurchaseResponsible());
        Purchase purchase = parseCommonPurchasePropsEA(data);
        purchase.setType(PurchaseType.ZA);
        purchase.setAuctionTime(data.getProcedureInfo().getBidding().getDate().toGregorianCalendar().getTime());
        purchase.setAuctionPlace(data.getProcedureInfo().getBidding().getPlace());
        purchase.setSubmissionCloseDate(data.getProcedureInfo().getCollecting().getEndDate()
                .toGregorianCalendar().getTime());
        purchase.setSubmissionOrder(data.getProcedureInfo().getCollecting().getOrder());
        purchase.setSubmissionPlace(data.getProcedureInfo().getCollecting().getPlace());
        purchase.setDocumentationDeliveryPlace(data.getProcedureInfo().getCollecting().getPlace());
        purchase.setDocumentationDeliveryProcedure(data.getProcedureInfo().getCollecting().getOrder());
        List<Preferense> preferenses = new ArrayList<>();
        for (ZfcsLotOKType lt: data.getLots().getLot()){
            if(lt.getPreferenses() != null)
                for (ZfcsPreferenseType pref : lt.getPreferenses().getPreferense()){
                    Preferense preferense = new Preferense();
                    preferense.setId(pref.getCode());
                    preferense.setName(pref.getName());
                    preferense.setValue(pref.getPrefValue());
                    preferense.setPurchase(purchase);
                    preferenses.add(preferense);
                }
            purchase.setPreferenses(preferenses);
        }
        purchase.setStartPrice(0);
        purchase.setApplicationGuarantee(Double.valueOf(data.getLots().getLot().get(0).getCustomerRequirements()
                .getCustomerRequirement().get(0).getApplicationGuarantee().getAmount()));
        purchase.setContractGuarantee(Double.valueOf(data.getLots().getLot().get(0).getCustomerRequirements()
                .getCustomerRequirement().get(0).getContractGuarantee().getAmount()));
        List<Lot> lots = new ArrayList<>();
        for (ZfcsLotOKType lt: data.getLots().getLot()){
            Lot lot = new Lot();
            lot.setId(data.getPurchaseNumber());
            lot.setPurchase(purchase);
            lot.setName(data.getPurchaseObjectInfo());
            lot.setAddress(lt.getCustomerRequirements().getCustomerRequirement().get(0)
                    .getKladrPlaces().getKladrPlace().get(0).getDeliveryPlace());
            lot.setStartPrice(Double.valueOf(lt.getPurchaseObjects().getTotalSum()));
            lot.setCurrency(lt.getCurrency().getCode());
            List<LotItem> lotItems = new ArrayList<>();
            int i = 0;
            for (ZfcsNotificationOKType.Lots.Lot.PurchaseObjects.PurchaseObject purchaseObject
                    : lt.getPurchaseObjects().getPurchaseObject()){
                LotItem lotItem = new LotItem();
                lotItem.setLot(lot);
                lotItem.setId(lot.getId()+(i++));
                if(purchaseObject.getName()!=null)
                    lotItem.setName(purchaseObject.getName());
                try {
                    lotItem.setCodeOkpd2(purchaseObject.getOKPD().getCode());
                }catch (Exception e){
                    lotItem.setCodeOkpd2(purchaseObject.getOKPD2().getCode());
                }
                if(purchaseObject.getOKEI() != null)
                    lotItem.setOkeiName(purchaseObject.getOKEI().getNationalCode());
                lotItem.setQty(purchaseObject.getQuantity().getValue());
                lotItem.setSum(Double.valueOf(purchaseObject.getSum()));
                if(purchaseObject.getPrice() != null)
                    lotItem.setPrice(Double.valueOf(purchaseObject.getPrice()));
                lotItems.add(lotItem);
            }
            lot.setLotItems(lotItems);
            lots.add(lot);
            purchase.setStartPrice(purchase.getStartPrice()+Double.valueOf(lt.getPurchaseObjects().getTotalSum()));
        }
        purchase.setLots(lots);
        purchase.setDocument(parseDocs(data.getAttachments(), purchase));
        purchase.setCustomer(customer);
        return purchase;
    }

    private Purchase parseZK(String pathToFile, String fileName) throws JAXBException {
        log.info("Запрос котировок - "+fileName);
        JAXBContext jc = JAXBContext.newInstance(Export.class);
        Unmarshaller unmarshaller = jc.createUnmarshaller();
        ZfcsNotificationZKType data = ((Export)unmarshaller.unmarshal(new File(pathToFile))).getFcsNotificationZK();
        Customer customer = parseCustomerEA(data.getPurchaseResponsible());
        Purchase purchase = parseCommonPurchasePropsEA(data);
        purchase.setType(PurchaseType.ZK);
        purchase.setSubmissionCloseDate(data.getProcedureInfo().getCollecting().getEndDate()
                .toGregorianCalendar().getTime());
        purchase.setSubmissionOrder(data.getProcedureInfo().getCollecting().getOrder());
        purchase.setSubmissionPlace(data.getProcedureInfo().getCollecting().getPlace());
        purchase.setQuotationExaminationPlace(data.getProcedureInfo().getOpening().getPlace());
        purchase.setQuotationExaminationTime(data.getProcedureInfo().getOpening().getDate().toGregorianCalendar().getTime());
        purchase.setDocumentationDeliveryPlace(data.getProcedureInfo().getCollecting().getPlace());
        purchase.setDocumentationDeliveryProcedure(data.getProcedureInfo().getCollecting().getOrder());
        List<Preferense> preferenses = new ArrayList<>();
        if(data.getLot().getPreferenses() != null)
            for (ZfcsPreferenseType pref : data.getLot().getPreferenses().getPreferense()){
                Preferense preferense = new Preferense();
                preferense.setId(pref.getCode());
                preferense.setName(pref.getName());
                preferense.setValue(pref.getPrefValue());
                preferense.setPurchase(purchase);
                preferenses.add(preferense);
            }
        purchase.setPreferenses(preferenses);
        purchase.setStartPrice(Double.valueOf(data.getLot().getPurchaseObjects().getTotalSum()));
        Lot lot = new Lot();
        lot.setId(data.getPurchaseNumber());
        lot.setPurchase(purchase);
        lot.setName(data.getPurchaseObjectInfo());
        lot.setAddress(data.getLot().getCustomerRequirements().getCustomerRequirement().get(0)
                .getKladrPlaces().getKladrPlace().get(0).getDeliveryPlace());
        lot.setStartPrice(Double.valueOf(data.getLot().getPurchaseObjects().getTotalSum()));
        lot.setCurrency(data.getLot().getCurrency().getCode());
        List<LotItem> lotItems = new ArrayList<>();
        int i = 0;
        for (ZfcsNotificationZKType.Lot.PurchaseObjects.PurchaseObject purchaseObject
                : data.getLot().getPurchaseObjects().getPurchaseObject()){
            LotItem lotItem = new LotItem();
            lotItem.setLot(lot);
            lotItem.setId(lot.getId()+(i++));
            if(purchaseObject.getName()!=null)
                lotItem.setName(purchaseObject.getName());
            try {
                lotItem.setCodeOkpd2(purchaseObject.getOKPD().getCode());
            }catch (Exception e){
                lotItem.setCodeOkpd2(purchaseObject.getOKPD2().getCode());
            }
            if(purchaseObject.getOKEI() != null)
                lotItem.setOkeiName(purchaseObject.getOKEI().getNationalCode());
            lotItem.setQty(purchaseObject.getQuantity().getValue());
            lotItem.setSum(Double.valueOf(purchaseObject.getSum()));
            if(purchaseObject.getPrice() != null)
                lotItem.setPrice(Double.valueOf(purchaseObject.getPrice()));
            lotItems.add(lotItem);
        }
        lot.setLotItems(lotItems);
        purchase.setLots(Collections.singletonList(lot));
        purchase.setDocument(parseDocs(data.getAttachments(), purchase));
        purchase.setCustomer(customer);
        return purchase;
    }

    private Purchase parseZP(String pathToFile, String fileName) throws JAXBException {
        log.info("Запрос предложений - "+fileName);
        JAXBContext jc = JAXBContext.newInstance(Export.class);
        Unmarshaller unmarshaller = jc.createUnmarshaller();
        ZfcsNotificationZPType data = ((Export)unmarshaller.unmarshal(new File(pathToFile))).getFcsNotificationZP();
        Customer customer = parseCustomerEA(data.getPurchaseResponsible());
        Purchase purchase = parseCommonPurchasePropsEA(data);
        purchase.setType(PurchaseType.ZP);
        purchase.setSubmissionCloseDate(data.getProcedureInfo().getCollecting().getEndDate()
                .toGregorianCalendar().getTime());
        purchase.setSubmissionOrder(data.getProcedureInfo().getCollecting().getOrder());
        purchase.setSubmissionPlace(data.getProcedureInfo().getCollecting().getPlace());
        purchase.setQuotationExaminationPlace(data.getProcedureInfo().getOpening().getPlace());
        purchase.setQuotationExaminationTime(data.getProcedureInfo().getOpening().getDate().toGregorianCalendar().getTime());
        purchase.setDocumentationDeliveryPlace(data.getProcedureInfo().getCollecting().getPlace());
        purchase.setDocumentationDeliveryProcedure(data.getProcedureInfo().getCollecting().getOrder());
        List<Preferense> preferenses = new ArrayList<>();
        if(data.getLot().getPreferenses() != null)
            for (ZfcsPreferenseType pref : data.getLot().getPreferenses().getPreferense()){
                Preferense preferense = new Preferense();
                preferense.setId(pref.getCode());
                preferense.setName(pref.getName());
                preferense.setValue(pref.getPrefValue());
                preferense.setPurchase(purchase);
                preferenses.add(preferense);
            }
        purchase.setPreferenses(preferenses);
        purchase.setStartPrice(Double.valueOf(data.getLot().getPurchaseObjects().getTotalSum()));
        purchase.setApplicationGuarantee(Double.valueOf(data.getLot().getCustomerRequirements()
                .getCustomerRequirement().get(0).getApplicationGuarantee().getAmount()));
        if(data.getLot().getCustomerRequirements().getCustomerRequirement().get(0).getContractGuarantee()!=null)
            purchase.setContractGuarantee(Double.valueOf(data.getLot().getCustomerRequirements()
                    .getCustomerRequirement().get(0).getContractGuarantee().getAmount()));
        Lot lot = new Lot();
        lot.setId(data.getPurchaseNumber());
        lot.setPurchase(purchase);
        lot.setName(data.getPurchaseObjectInfo());
        lot.setAddress(data.getLot().getCustomerRequirements().getCustomerRequirement().get(0)
                .getKladrPlaces().getKladrPlace().get(0).getDeliveryPlace());
        lot.setStartPrice(Double.valueOf(data.getLot().getPurchaseObjects().getTotalSum()));
        lot.setCurrency(data.getLot().getCurrency().getCode());
        List<LotItem> lotItems = new ArrayList<>();
        int i = 0;
        for (ZfcsNotificationZPType.Lot.PurchaseObjects.PurchaseObject purchaseObject
                : data.getLot().getPurchaseObjects().getPurchaseObject()){
            LotItem lotItem = new LotItem();
            lotItem.setLot(lot);
            lotItem.setId(lot.getId()+(i++));
            if(purchaseObject.getName()!=null)
                lotItem.setName(purchaseObject.getName());
            try {
                lotItem.setCodeOkpd2(purchaseObject.getOKPD().getCode());
            }catch (Exception e){
                lotItem.setCodeOkpd2(purchaseObject.getOKPD2().getCode());
            }
            if(purchaseObject.getOKEI() != null)
                lotItem.setOkeiName(purchaseObject.getOKEI().getNationalCode());
            lotItem.setQty(purchaseObject.getQuantity().getValue());
            lotItem.setSum(Double.valueOf(purchaseObject.getSum()));
            if(purchaseObject.getPrice() != null)
                lotItem.setPrice(Double.valueOf(purchaseObject.getPrice()));
            lotItems.add(lotItem);
        }
        lot.setLotItems(lotItems);
        purchase.setLots(Collections.singletonList(lot));
        purchase.setDocument(parseDocs(data.getAttachments(), purchase));
        purchase.setCustomer(customer);
        return purchase;
    }

    private Purchase parseCommonPurchasePropsEA(ZfcsPurchaseNotificationType data){
        Purchase purchase = new Purchase();
        purchase.setEventDate(data.getDocPublishDate().toGregorianCalendar().getTime());
        purchase.setId(data.getPurchaseNumber());
        purchase.setName(data.getPurchaseObjectInfo());
        purchase.setFz("44");
        purchase.setStartPrice(0);
        purchase.setPublicationDate(data.getDocPublishDate().toGregorianCalendar().getTime());
        purchase.setContactFirstName(data.getPurchaseResponsible().getResponsibleInfo().getContactPerson().getFirstName());
        purchase.setContactMiddleName(data.getPurchaseResponsible().getResponsibleInfo().getContactPerson().getMiddleName());
        purchase.setContactLastName(data.getPurchaseResponsible().getResponsibleInfo().getContactPerson().getLastName());
        purchase.setContactPhone(data.getPurchaseResponsible().getResponsibleInfo().getContactPhone());
        purchase.setContactEmail(data.getPurchaseResponsible().getResponsibleInfo().getContactEMail());
        purchase.setPurchaseCodeName(data.getPlacingWay().getName());
        return purchase;
    }

    private Purchase parseCommonPurchasePropsEP(ZfcsNotificationEPType data){
        Purchase purchase = new Purchase();
        purchase.setEventDate(data.getDocPublishDate().toGregorianCalendar().getTime());
        purchase.setId(data.getPurchaseNumber());
        purchase.setName(data.getPurchaseObjectInfo());
        purchase.setFz("44");
        purchase.setStartPrice(0);
        purchase.setPublicationDate(data.getDocPublishDate().toGregorianCalendar().getTime());
        purchase.setContactFirstName(data.getPurchaseResponsible().getResponsibleInfo().getContactPerson().getFirstName());
        purchase.setContactMiddleName(data.getPurchaseResponsible().getResponsibleInfo().getContactPerson().getMiddleName());
        purchase.setContactLastName(data.getPurchaseResponsible().getResponsibleInfo().getContactPerson().getLastName());
        purchase.setContactPhone(data.getPurchaseResponsible().getResponsibleInfo().getContactPhone());
        purchase.setContactEmail(data.getPurchaseResponsible().getResponsibleInfo().getContactEMail());
        purchase.setPurchaseCodeName(data.getPlacingWay().getName());
        return purchase;
    }

    private Customer parseCustomerEA(ZfcsPurchaseNotificationType.PurchaseResponsible customerInfo){
        Customer customer = new Customer();
        customer.setId(Long.parseLong(customerInfo.getResponsibleOrg().getINN()));
        if((customerInfo.getResponsibleOrg().getFullName()==null)
                ||(customerInfo.getResponsibleOrg().getFullName().isEmpty())){
            customer.setName(customerInfo.getResponsibleOrg().getShortName());
        }else{
            customer.setName(customerInfo.getResponsibleOrg().getFullName());
        }
        customer.setPhone(customerInfo.getResponsibleInfo().getContactPhone());
        if((customerInfo.getResponsibleOrg().getPostAddress()==null)
                ||(customerInfo.getResponsibleOrg().getPostAddress().isEmpty())){
            customer.setPostalAddress(customerInfo.getResponsibleOrg().getFactAddress());
        }else{
            customer.setPostalAddress(customerInfo.getResponsibleOrg().getPostAddress());
        }
        customer.setEmail(customerInfo.getResponsibleInfo().getContactEMail());
        customer.setFax(customerInfo.getResponsibleInfo().getContactFax());
        return customer;
    }

    private Customer parseCustomerEP(ZfcsNotificationEPType.PurchaseResponsible customerInfo){
        Customer customer = new Customer();
        customer.setId(Long.parseLong(customerInfo.getResponsibleOrg().getINN()));
        if((customerInfo.getResponsibleOrg().getFullName()==null)
                ||(customerInfo.getResponsibleOrg().getFullName().isEmpty())){
            customer.setName(customerInfo.getResponsibleOrg().getShortName());
        }else{
            customer.setName(customerInfo.getResponsibleOrg().getFullName());
        }
        customer.setPhone(customerInfo.getResponsibleInfo().getContactPhone());
        if((customerInfo.getResponsibleOrg().getPostAddress()==null)
                ||(customerInfo.getResponsibleOrg().getPostAddress().isEmpty())){
            customer.setPostalAddress(customerInfo.getResponsibleOrg().getFactAddress());
        }else{
            customer.setPostalAddress(customerInfo.getResponsibleOrg().getPostAddress());
        }
        customer.setEmail(customerInfo.getResponsibleInfo().getContactEMail());
        customer.setFax(customerInfo.getResponsibleInfo().getContactFax());
        return customer;
    }

    private List<su.asgor.model.Document> parseDocs(ZfcsAttachmentListType data, Purchase purchase){
        List<su.asgor.model.Document> documents = new ArrayList<>();
        if(data!=null && data.getAttachment()!=null)
            for(ZfcsAttachmentType doc:data.getAttachment()){
                su.asgor.model.Document document = new su.asgor.model.Document();
                document.setName(doc.getFileName());
                document.setUrl(doc.getUrl());
                document.setPurchase(purchase);
                documents.add(document);
            }
        return documents;
    }
}
