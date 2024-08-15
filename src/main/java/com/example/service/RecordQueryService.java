package com.example.service;

import com.example.entity.Record;
import com.example.repository.RecordRepository;
import com.google.gson.Gson;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class RecordQueryService {
    @Autowired
    private RecordRepository repository;

    @Autowired
    private EntityManager entityManager;

    // Define a GET endpoint
    @GetMapping("/query")
    public String sayHello() {
        Query query = entityManager.createNativeQuery("SELECT * FROM RECORD WHERE (SELLER_PARTY = ? AND CURRENCY = ?) OR (SELLER_PARTY = ? AND CURRENCY = ?) ", Record.class);
        query.setParameter(1, "EMU_BANK");
        query.setParameter(2, "AUD");

        query.setParameter(3, "BISON_BANK");
        query.setParameter(4, "USD");

        var resultList = (List<Record>) query.getResultList();

        if (resultList.size() > 0) {
            // filter out the buyer and seller if they are anagrams of each other
            resultList = resultList.stream()
                    .filter(rec -> notAnagrams(rec.getBuyerParty(), rec.getSellerParty()))
                    .collect(Collectors.toList());

        }
        return rootGson.toJson(resultList);
    }

    // Time Complexity of the method is O(n log n).
    // just did a search and took this piece of code from the internet
    // https://github.com/AbdurRKhalid/Java-Anagrams-Checker
    // did a quick analysis of the algo and it looks good, but the simplest way of checking.
    private static boolean notAnagrams(String string1, String string2) {
        // Checking if Both Strings are of Same Length.
        if(string1.length() != string2.length()) {
            return true;
        }
        // Converting each String to Array of Characters.
        char[] chars1 = string1.toCharArray();
        char[] chars2 = string2.toCharArray();

        // Sorting Each Array of Characters.
        Arrays.sort(chars1);
        Arrays.sort(chars2);

        // If Both Sorted Arrays are Equal then Both Strings are Anagrams.
        return !Arrays.equals(chars1, chars2);
    }

    static Gson rootGson = new Gson();
}
