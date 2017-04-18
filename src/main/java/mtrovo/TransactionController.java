package mtrovo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.net.URI;

@RestController
public class TransactionController {

    @Autowired
    private TransactionService service;

    @RequestMapping("/transactions")
    public ResponseEntity<?> createTransaction(@Valid @RequestBody Transaction transaction) {
        service.addTransaction(transaction);
        return ResponseEntity.status(HttpStatus.CREATED).body(null);
    }

    @RequestMapping("/statistics")
    public Stats getStats() {
        return service.getStats();
    }

}

