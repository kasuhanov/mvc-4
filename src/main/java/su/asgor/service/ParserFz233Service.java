package su.asgor.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import su.asgor.dao.CustomerRepository;
import su.asgor.dao.PurchaseRepository;
import su.asgor.model.*;
import su.asgor.parser.generated.fz223.*;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
@Transactional
public class ParserFz233Service {
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
        	if(fileName.startsWith("purchaseNoticeAE")){
                correctFileName = true;
                purchase = parseAE(pathToFile,fileName);
            }
            if(fileName.startsWith("purchaseNoticeEP")){
                correctFileName = true;
                purchase = parseEP(pathToFile,fileName);
            }
            if(fileName.startsWith("purchaseNoticeOA")){
                correctFileName = true;
                purchase = parseOA(pathToFile,fileName);
            }
            if(fileName.startsWith("purchaseNoticeZK")){
                correctFileName = true;
                purchase = parseZK(pathToFile,fileName);
            }
            if(fileName.startsWith("purchaseNoticeOK")){
                correctFileName = true;
                purchase = parseOK(pathToFile,fileName);
            }
            if(!correctFileName){
                throw new Exception("invalid fileName");
            }
            switch (purchase.getStatus()) {
            case F:
            case M:
                purchase.setStatus(PurchaseNoticeStatusType.P);
                log.info("F or M type, purchase №"+purchase.getId());
                break;
			case I:
				log.info("I type, purchase №"+purchase.getId());
            	Purchase purchaseInDB = purchaseRepository.findOne(purchase.getId());
            	purchaseInDB.setCompleted(true);
            	purchaseRepository.save(purchaseInDB);
				break;
			case P:
				log.info("P type, purchase №"+purchase.getId());
				customerRepository.save(purchase.getCustomer());
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
                    purchase.setAfter(true);
                }
                purchase.setFileName(fileName);
				purchaseRepository.save(categoryService.setupCategory(purchase));
                break;
			}
            return purchase;
		} catch (Exception e) {
			log.error("exception in file:"+fileName+" Messaage:"+e.getMessage(),e);
			throw new Exception(e);
		}
    }

    private Purchase parseAE(String pathToFile, String fileName) throws JAXBException {
        log.info("Извещения об аукционах в электронном виде - "+fileName);
        JAXBContext jc = JAXBContext.newInstance(PurchaseNoticeAE.class);
        Unmarshaller unmarshaller = jc.createUnmarshaller();
        PurchaseNoticeAE purchaseNoticeAE = (PurchaseNoticeAE)unmarshaller.unmarshal(new File(pathToFile));
        PurchaseNoticeAEDataType data = purchaseNoticeAE.getBody().getItem().getPurchaseNoticeAEData();
        Customer customer = parseCustomer(data.getCustomer().getMainInfo());        
        Purchase purchase = parseCommonPurchaseProps(data);
        purchase.setEventDate(purchaseNoticeAE.getHeader().getCreateDateTime().toGregorianCalendar().getTime());
        purchase.setType(PurchaseType.AE);
        purchase.setSubmissionCloseDate(data.getSubmissionCloseDateTime().toGregorianCalendar().getTime());
        purchase.setAuctionTime(data.getAuctionTime().toGregorianCalendar().getTime());
        if(purchase.getAuctionTime().before(new Date()))
        	purchase.setCompleted(true);
        purchase.setElectronicPlaceName(data.getElectronicPlaceInfo().getName());
        purchase.setElectronicPlaceUrl(
            data.getElectronicPlaceInfo().getUrl().startsWith("http") ?
                data.getElectronicPlaceInfo().getUrl() : "http://" + data.getElectronicPlaceInfo().getUrl()
        );
        purchase.setLots(parseLots(data.getLots(), purchase));
        purchase.setDocument(parseDocs(data.getAttachments(), purchase));
        purchase.setCustomer(customer);
        return purchase;
    }

    private Purchase parseEP(String pathToFile, String fileName) throws JAXBException {
    	log.info("Извещения о закупках у единственного поставщика - "+fileName);
        JAXBContext jc = JAXBContext.newInstance(PurchaseNoticeEP.class);
        Unmarshaller unmarshaller = jc.createUnmarshaller();
        PurchaseNoticeEP purchaseNoticeEP = (PurchaseNoticeEP)unmarshaller.unmarshal(new File(pathToFile));
        PurchaseNoticeEPDataType data = purchaseNoticeEP.getBody().getItem().getPurchaseNoticeEPData();
        Customer customer = parseCustomer(data.getCustomer().getMainInfo());  
        Purchase purchase = parseCommonPurchaseProps(data);
        purchase.setEventDate(purchaseNoticeEP.getHeader().getCreateDateTime().toGregorianCalendar().getTime());
        purchase.setType(PurchaseType.EP);
        purchase.setCompleted(true);
        purchase.setLots(parseLots(data.getLots(), purchase));
        purchase.setDocument(parseDocs(data.getAttachments(), purchase));
        purchase.setCustomer(customer);
        return purchase;
    }

    private Purchase parseOA(String pathToFile, String fileName) throws JAXBException {
    	log.info("Извещения об открытых аукционах - "+fileName);
        JAXBContext jc = JAXBContext.newInstance(PurchaseNoticeOA.class);
        Unmarshaller unmarshaller = jc.createUnmarshaller();
        PurchaseNoticeOA purchaseNoticeOA = (PurchaseNoticeOA)unmarshaller.unmarshal(new File(pathToFile));
        PurchaseNoticeOADataType data = purchaseNoticeOA.getBody().getItem().getPurchaseNoticeOAData();
        Customer customer = parseCustomer(data.getCustomer().getMainInfo());
        Purchase purchase = parseCommonPurchaseProps(data);
        purchase.setEventDate(purchaseNoticeOA.getHeader().getCreateDateTime().toGregorianCalendar().getTime());
        purchase.setType(PurchaseType.OA);
        purchase.setSubmissionCloseDate(data.getSubmissionCloseDateTime().toGregorianCalendar().getTime());
        purchase.setAuctionPlace(data.getAuctionPlace());
        purchase.setAuctionTime(data.getAuctionTime().toGregorianCalendar().getTime());
        if(purchase.getAuctionTime().before(new Date()))
        	purchase.setCompleted(true);
        purchase.setLots(parseLots(data.getLots(), purchase));
        purchase.setDocument(parseDocs(data.getAttachments(), purchase));
        purchase.setCustomer(customer);
        return purchase;
    }

    private Purchase parseOK(String pathToFile, String fileName) throws JAXBException {
    	log.info("Извещения об открытых конкурсах - "+fileName);
        JAXBContext jc = JAXBContext.newInstance(PurchaseNoticeOK.class);
        Unmarshaller unmarshaller = jc.createUnmarshaller();
        PurchaseNoticeOK purchaseNoticeOK = (PurchaseNoticeOK)unmarshaller.unmarshal(new File(pathToFile));
        PurchaseNoticeOKDataType data = purchaseNoticeOK.getBody().getItem().getPurchaseNoticeOKData();
        Customer customer = parseCustomer(data.getCustomer().getMainInfo());
        Purchase purchase = parseCommonPurchaseProps(data);
        purchase.setEventDate(purchaseNoticeOK.getHeader().getCreateDateTime().toGregorianCalendar().getTime());
        purchase.setType(PurchaseType.OK);
        purchase.setSubmissionCloseDate(data.getSubmissionCloseDateTime().toGregorianCalendar().getTime());
        purchase.setEnvelopeOpeningPlace(data.getEnvelopeOpeningPlace());
        purchase.setExaminationPlace(data.getExaminationPlace());
        purchase.setSummingupPlace(data.getSummingupPlace());
        purchase.setExaminationDateTime(data.getExaminationDateTime().toGregorianCalendar().getTime());
        purchase.setEnvelopeOpeningTime(data.getEnvelopeOpeningTime().toGregorianCalendar().getTime());
        purchase.setSummingupTime(data.getSummingupTime().toGregorianCalendar().getTime());
        if(purchase.getSummingupTime().before(new Date()))
        	purchase.setCompleted(true);
        purchase.setLots(parseLots(data.getLots(), purchase));
        purchase.setDocument(parseDocs(data.getAttachments(), purchase));
        purchase.setCustomer(customer);
        return purchase;
    }

    private Purchase parseZK(String pathToFile, String fileName) throws JAXBException {
    	log.info("Извещения о запросах котировок - "+fileName);
        JAXBContext jc = JAXBContext.newInstance(PurchaseNoticeZK.class);
        Unmarshaller unmarshaller = jc.createUnmarshaller();
        PurchaseNoticeZK purchaseNoticeZK = (PurchaseNoticeZK)unmarshaller.unmarshal(new File(pathToFile));
        PurchaseNoticeZKDataType data = purchaseNoticeZK.getBody().getItem().getPurchaseNoticeZKData();
        Customer customer = parseCustomer(data.getCustomer().getMainInfo());
        Purchase purchase = parseCommonPurchaseProps(data);
        purchase.setEventDate(purchaseNoticeZK.getHeader().getCreateDateTime().toGregorianCalendar().getTime());
        purchase.setType(PurchaseType.ZK);
        if(data.getSubmissionCloseDateTime()!=null)
            purchase.setSubmissionCloseDate(data.getSubmissionCloseDateTime().toGregorianCalendar().getTime());
        purchase.setQuotationExaminationPlace(data.getQuotationExaminationPlace());
        purchase.setQuotationExaminationTime(data.getQuotationExaminationTime().toGregorianCalendar().getTime());
        if(purchase.getQuotationExaminationTime().before(new Date()))
        	purchase.setCompleted(true);
        purchase.setLots(parseLots(data.getLots(), purchase));
        purchase.setDocument(parseDocs(data.getAttachments(), purchase));
        purchase.setCustomer(customer);
        return purchase;
    }

    private Purchase parseCommonPurchaseProps(PurchaseNoticeNonISBaseType data){
        Purchase purchase = new Purchase();
        purchase.setId(data.getRegistrationNumber());
        purchase.setStatus(data.getStatus());
        purchase.setName(data.getName());
        purchase.setFz("223");
        purchase.setStartPrice(0);
        purchase.setPublicationDate(data.getPublicationDateTime().toGregorianCalendar().getTime());
        purchase.setDocumentationDeliveryPlace(data.getDocumentationDelivery().getPlace());
        purchase.setDocumentationDeliveryProcedure(data.getDocumentationDelivery().getProcedure());
        purchase.setContactFirstName(data.getContact().getFirstName());
        purchase.setContactMiddleName(data.getContact().getMiddleName());
        purchase.setContactLastName(data.getContact().getLastName());
        purchase.setContactPhone(data.getContact().getPhone());
        purchase.setContactEmail(data.getContact().getEmail());
        purchase.setPurchaseCodeName(data.getPurchaseCodeName());
        return purchase;
    }

    private Customer parseCustomer(CustomerMainInfoType customerInfo){
    	Customer customer = new Customer();
    	customer.setId(Long.parseLong(customerInfo.getInn()));
        if((customerInfo.getFullName()==null)||(customerInfo.getFullName().isEmpty())){
        	customer.setName(customerInfo.getShortName());
        }else{
        	customer.setName(customerInfo.getFullName());
        }
        customer.setPhone(customerInfo.getPhone());
        if((customerInfo.getPostalAddress()==null)||(customerInfo.getPostalAddress().isEmpty())){
        	customer.setPostalAddress(customerInfo.getLegalAddress());
        }else{
        	customer.setPostalAddress(customerInfo.getPostalAddress());
        }
        customer.setEmail(customerInfo.getEmail());
        customer.setFax(customerInfo.getFax());
    	return customer;
    }

    private List<Lot> parseLots(LotListType lotData, Purchase purchase){
    	List<Lot> lotList = new ArrayList<>();
        for(LotType lt:lotData.getLot()){
            Lot lot = new Lot();
            lot.setId(lt.getGuid());
            lot.setPurchase(purchase);
            if(lt.getLotData() == null)
                throw new NullPointerException("lot data == null");
            lot.setName(lt.getLotData().getSubject());
            if(lt.getLotData().getDeliveryPlace() != null)
                lot.setAddress(lt.getLotData().getDeliveryPlace().getAddress());
            lot.setStartPrice(lt.getLotData().getInitialSum().doubleValue());
            purchase.setStartPrice(purchase.getStartPrice()+lot.getStartPrice());
            if(lt.getLotData().isSubcontractorsRequirement()==null||lt.getLotData().isForSmallOrMiddle()==null){
                if(lt.getLotData().isSubcontractorsRequirement()==null&&lt.getLotData().isForSmallOrMiddle()==null){
                    lot.setForSMP(null);
                }else{
                    if(lt.getLotData().isForSmallOrMiddle()==null){
                        lot.setForSMP(lt.getLotData().isSubcontractorsRequirement());
                    }
                    if(lt.getLotData().isSubcontractorsRequirement()==null){
                        lot.setForSMP(lt.getLotData().isForSmallOrMiddle());
                    }
                }
            }
            else{
                    lot.setForSMP(lt.getLotData().isForSmallOrMiddle()||lt.getLotData().isSubcontractorsRequirement());
            }
            lot.setCurrency(lt.getLotData().getCurrency().getCode());
            List<LotItem> lotItems = new ArrayList<>();
            for(LotItemType li:lt.getLotData().getLotItems().getLotItem()){
                LotItem  lotItem = new LotItem();
                try{
                    lotItem.setCodeOkdp(li.getOkdp().getCode());
                    lotItem.setCodeOkved(li.getOkved().getCode());
                }catch (Exception e){
                    lotItem.setCodeOkpd2(li.getOkpd2().getCode());
                    lotItem.setCodeOkved2(li.getOkved2().getCode());
                }
                lotItem.setLot(lot);
                try{lotItem.setOkeiName(li.getOkei().getName());}catch (Exception ignored){}
                lotItem.setOrdinalNumber(li.getOrdinalNumber());
                lotItem.setQty(li.getQty());
                lotItem.setAdditionalInfo(li.getAdditionalInfo());
                lotItem.setId(li.getGuid());
                lotItems.add(lotItem);
            }
            lot.setLotItems(lotItems);
            lotList.add(lot);
        }
    	return lotList;
    }

    private List<Document> parseDocs(DocumentListType data, Purchase purchase){
    	List<Document> documents = new ArrayList<>();
        for(DocumentType doc:data.getDocument()){
            Document document = new Document();
            document.setName(doc.getFileName());
            document.setUrl(doc.getUrl());
            document.setPurchase(purchase);
            documents.add(document);
        }
        return documents;
    }
}
