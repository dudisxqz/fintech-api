package br.com.fintech.API.account.controller;

import br.com.fintech.API.account.model.Account;
import br.com.fintech.API.account.model.DTO.AccountResponseDTO;
import br.com.fintech.API.account.model.DTO.CreateAccountRequest;
import br.com.fintech.API.account.model.DTO.UpdateAccountRequest;
import br.com.fintech.API.account.service.AccountService;
import br.com.fintech.API.infra.security.SecurityUtils;
import br.com.fintech.API.user.model.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static br.com.fintech.API.infra.config.APIPaths.ACCOUNT;

@RestController
@RequestMapping(ACCOUNT)
@Tag(name = "Account's Management", description = "Operations for creating, updating and retrieving accounts")
public class AccountController {

    @Autowired
    private SecurityUtils securityUtils;

    @Autowired
    private AccountService accountService;

    @GetMapping
    @Operation(summary = "Get accounts by the user of the JWT given")
    public ResponseEntity<Page<AccountResponseDTO>> getAccounts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        User user = securityUtils.getCurrentUser();

        Pageable pageable = PageRequest.of(page, size);
        Page<AccountResponseDTO> accountsPage = accountService
                .getAccountsByUserId(user.getId(), pageable)
                .map(a -> new AccountResponseDTO(a.getId(), a.getName()));
        return ResponseEntity.ok(accountsPage);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update account name")
    public ResponseEntity<AccountResponseDTO> updateAccount(@PathVariable String id, @Valid @RequestBody UpdateAccountRequest request) {
        Account account = accountService.update(id, request);
        return ResponseEntity.ok(new AccountResponseDTO(account.getId(), account.getName()));
    }

    @PostMapping
    @Operation(summary = "Creating a new account for the user logged in")
    public ResponseEntity<AccountResponseDTO> createAccount(@Valid @RequestBody CreateAccountRequest request) {
        User user = securityUtils.getCurrentUser();
        Account account = accountService.create(user.getId(),request);
        return ResponseEntity.status(HttpStatus.CREATED).body(new AccountResponseDTO(account.getId(), account.getName(), account.getBalance(), account.getCreatedAt()));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Deleting an account by its id")
    public ResponseEntity<AccountResponseDTO> deleteAccount(@PathVariable String id) {
        accountService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
