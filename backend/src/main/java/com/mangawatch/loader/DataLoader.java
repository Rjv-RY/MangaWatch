//package com.mangawatch.loader;
//
//import com.fasterxml.jackson.core.type.TypeReference;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.mangawatch.model.Manga;
//import com.mangawatch.repository.MangaRepository;
//import org.springframework.boot.CommandLineRunner;
//import org.springframework.core.io.ClassPathResource;
//import org.springframework.stereotype.Component;
//
//import java.io.InputStream;
//import java.util.List;
//
////Loads data from json to postgres
////Goes thru json -> loader -> JPArepository(or whatever extends it) --v
////-> data jpa + hibernate -> postgres
//
//@Component
//public class DataLoader implements CommandLineRunner {
//
//    private final MangaRepository mangaRepository;
//    private final ObjectMapper mapper = new ObjectMapper();
//
//    public DataLoader(MangaRepository mangaRepository) {
//        this.mangaRepository = mangaRepository;
//    }
//
//    @Override
//    public void run(String... args) throws Exception {
//        if (mangaRepository.count() > 0) {
//            // already loaded
//            return;
//        }
//
//        try (InputStream is = new ClassPathResource("data/mangas.json").getInputStream()) {
//            List<Manga> list = mapper.readValue(is, new TypeReference<List<Manga>>() {});
//            mangaRepository.saveAll(list);
//            System.out.println("✅ Loaded " + list.size() + " mangas into PostGres");
//        }
//    }
//}
