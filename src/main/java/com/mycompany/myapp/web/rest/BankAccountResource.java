package com.mycompany.myapp.web.rest;

import com.codahale.metrics.annotation.Timed;
import com.mycompany.myapp.domain.BankAccount;
import com.mycompany.myapp.repository.BankAccountRepository;
import com.mycompany.myapp.repository.search.BankAccountSearchRepository;
import com.mycompany.myapp.web.rest.util.HeaderUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import javax.validation.Valid;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static org.elasticsearch.index.query.QueryBuilders.*;

/**
 * REST controller for managing BankAccount.
 */
@RestController
@RequestMapping("/api")
public class BankAccountResource {

    private final Logger log = LoggerFactory.getLogger(BankAccountResource.class);

    @Inject
    private BankAccountRepository bankAccountRepository;

    @Inject
    private BankAccountSearchRepository bankAccountSearchRepository;

    /**
     * POST  /bankAccounts -> Create a new bankAccount.
     */
    @RequestMapping(value = "/bankAccounts",
            method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<BankAccount> create(@Valid @RequestBody BankAccount bankAccount) throws URISyntaxException {
        log.debug("REST request to save BankAccount : {}", bankAccount);
        if (bankAccount.getId() != null) {
            return ResponseEntity.badRequest().header("Failure", "A new bankAccount cannot already have an ID").body(null);
        }
        BankAccount result = bankAccountRepository.save(bankAccount);
        bankAccountSearchRepository.save(result);
        return ResponseEntity.created(new URI("/api/bankAccounts/" + result.getId()))
                .headers(HeaderUtil.createEntityCreationAlert("bankAccount", result.getId().toString()))
                .body(result);
    }

    /**
     * PUT  /bankAccounts -> Updates an existing bankAccount.
     */
    @RequestMapping(value = "/bankAccounts",
        method = RequestMethod.PUT,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<BankAccount> update(@Valid @RequestBody BankAccount bankAccount) throws URISyntaxException {
        log.debug("REST request to update BankAccount : {}", bankAccount);
        if (bankAccount.getId() == null) {
            return create(bankAccount);
        }
        BankAccount result = bankAccountRepository.save(bankAccount);
        bankAccountSearchRepository.save(bankAccount);
        return ResponseEntity.ok()
                .headers(HeaderUtil.createEntityUpdateAlert("bankAccount", bankAccount.getId().toString()))
                .body(result);
    }

    /**
     * GET  /bankAccounts -> get all the bankAccounts.
     */
    @RequestMapping(value = "/bankAccounts",
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public List<BankAccount> getAll() {
        log.debug("REST request to get all BankAccounts");
        return bankAccountRepository.findAll();
    }

    /**
     * GET  /bankAccounts/:id -> get the "id" bankAccount.
     */
    @RequestMapping(value = "/bankAccounts/{id}",
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<BankAccount> get(@PathVariable Long id) {
        log.debug("REST request to get BankAccount : {}", id);
        return Optional.ofNullable(bankAccountRepository.findOne(id))
            .map(bankAccount -> new ResponseEntity<>(
                bankAccount,
                HttpStatus.OK))
            .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    /**
     * DELETE  /bankAccounts/:id -> delete the "id" bankAccount.
     */
    @RequestMapping(value = "/bankAccounts/{id}",
            method = RequestMethod.DELETE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        log.debug("REST request to delete BankAccount : {}", id);
        bankAccountRepository.delete(id);
        bankAccountSearchRepository.delete(id);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert("bankAccount", id.toString())).build();
    }

    /**
     * SEARCH  /_search/bankAccounts/:query -> search for the bankAccount corresponding
     * to the query.
     */
    @RequestMapping(value = "/_search/bankAccounts/{query}",
        method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public List<BankAccount> search(@PathVariable String query) {
        return StreamSupport
            .stream(bankAccountSearchRepository.search(queryString(query)).spliterator(), false)
            .collect(Collectors.toList());
    }
}
