package su.asgor.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import su.asgor.dao.CategoryRepository;
import su.asgor.dao.PatternRepository;
import su.asgor.dao.PurchaseRepository;
import su.asgor.model.*;

import java.util.*;

@Service
@Transactional
public class CategoryService {
	private final Logger log = LoggerFactory.getLogger(this.getClass());
    @Autowired
    private CategoryRepository categoryRepository;
    @Autowired
    private PatternRepository patternRepository;
    @Autowired
    private PurchaseRepository purchaseRepository;
    private HashMap<String,List<String>> map;
    private List<Category> categories = new ArrayList<>();
    private List<Pattern> patterns = new ArrayList<>();

    public Purchase setupCategory(Purchase purchase){
        List<Pattern> patterns = patternRepository.findAll();
        log.info("updating catgories of purchase №"+purchase.getId());
        GregorianCalendar okpd2Date = new GregorianCalendar(2016,1,1);
        if(purchase.getPublicationDate().after(okpd2Date.getTime())){
            for(Lot lot:purchase.getLots()){
                for (LotItem lotItem:lot.getLotItems()){
                    for(Pattern pattern:patterns){
                        if (lotItem.getCodeOkpd2().equals(pattern.getPattern())
                                ||(pattern.getPattern().contains("x")&&lotItem.getCodeOkpd2().startsWith(pattern.getPattern()
                                .substring(0, pattern.getPattern().lastIndexOf("x"))))){
                            for (Category category:pattern.getCategories()){
                                if(!purchase.getCategories().contains(category)){
                                    purchase.getCategories().add(category);
                                }
                            }
                        }
                    }
                }
            }
        }
        return purchase;
    }

    public void updatePurchasesCategory(){
        log.info("starting upd");
        List<Pattern> patterns = patternRepository.findAll();
        List<Purchase> purchases = purchaseRepository.findAll();
        for(Purchase purchase:purchases){
            log.info("updating purchase №"+purchase.getId());
            purchase.getCategories().clear();
            for(Lot lot:purchase.getLots()){
                for (LotItem lotItem:lot.getLotItems()){
                    if(lotItem.getCodeOkpd2()==null)
                        continue;
                    for(Pattern pattern:patterns){
                        if (lotItem.getCodeOkpd2().equals(pattern.getPattern())
                                ||(pattern.getPattern().contains("x")&&lotItem.getCodeOkpd2().startsWith(pattern.getPattern()
                                .substring(0, pattern.getPattern().lastIndexOf("x"))))){
                            for (Category category:pattern.getCategories()){
                                if(!purchase.getCategories().contains(category)){
                                    purchase.getCategories().add(category);
                                }
                            }
                        }
                    }
                }
            }
        }
        purchaseRepository.save(purchases);
        setupCount();
    }

    public void initialize(){
        if(categoryRepository.count() == 0){
            log.info("initializing patterns");
            initMap();
            initPatterns();
            for(Category category:categories){
                Category categoryInDb = categoryRepository.findByName(category.getName());
                if (categoryInDb != null){
                    for(Pattern pattern:category.getPatterns()){
                        if(!categoryInDb.getPatterns().contains(pattern)){
                            categoryInDb.addPattern(pattern);
                        }
                    }
                    categoryRepository.save(categoryInDb);
                }else {
                    categoryRepository.save(category);
                }
            }
        }
    }

    private void initPatterns(){
        for(Map.Entry<String,List<String>> entry:map.entrySet()){
            Category category = new Category(entry.getKey());
            categories.add(category);
            for(String p :entry.getValue()){
                Pattern pattern = new Pattern(p);
                if(patterns.contains(pattern)){
                    pattern = patterns.get(patterns.lastIndexOf(pattern));
                }else {
                    patterns.add(pattern);
                }
                category.addPattern(pattern);
            }
        }
    }

    private void initMap(){
        map = new HashMap<>();
        map.put("Благоустройство, вывоз мусора, утилизация", Arrays.asList("43.9x","38x","39x"));
        map.put("Исследование и проектирование", Arrays.asList("41.1x","70x","71x"));
        map.put("ИТ и телекоммуникации", Arrays.asList("62x","63x"));
        map.put("Канцтовары, офис, оргтехника", Arrays.asList("17x","26.20x"));
        map.put("Культура, искусство", Arrays.asList("90x","91x","93.2x"));
        map.put("Мебель", Collections.singletonList("31x"));
        map.put("Медицина, фармацевтика", Arrays.asList("21x","26.60x","32.50x"));
        map.put("Наука и образование", Arrays.asList("72x","85x"));
        map.put("Недвижимость", Collections.singletonList("68x"));
        map.put("Охрана, сигнализация", Arrays.asList("26.30x","80x","84.24x"));
        map.put("Продукты питания", Arrays.asList("10x","11x","56x"));
        map.put("Промышленное, торговое и иное оборудование", Arrays.asList("27x","28x","33x"));
        map.put("Реклама, полиграфия, СМИ", Arrays.asList("18x","58.1x","60x","73x"));
        map.put("Сельское хозяйство, биоресурсы", Collections.singletonList("01x"));
        map.put("Спорт, отдых, туризм", Arrays.asList("32.30x","77.21x","93.1x","79x"));
        map.put("Строительство", Arrays.asList("42x","43x"));
        map.put("Текстиль, одежда, обувь", Arrays.asList("13x","14x","15x"));
        map.put("Топливо, энергоносители, ГСМ", Arrays.asList("05x","06x","19x"));
        map.put("Транспорт, логистика, обслуживание", Arrays.asList("29x","30x","49x","50x","51x","52x"));
        map.put("Услуги по техническому обслуживанию", Arrays.asList("81x","82x"));
        map.put("Финансы, управление, право, страхование", Arrays.asList("64x","65x","66x","69x","70x"));
        map.put("Химическая продукция", Collections.singletonList("20x"));
    }

    public void setupCount(){
        List<Category> categories = categoryRepository.findAll();
        for (Category category:categories){
            category.setCount(category.getPurchases().size());
        }
    }
}
