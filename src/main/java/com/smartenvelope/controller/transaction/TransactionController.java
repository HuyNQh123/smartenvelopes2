package com.smartenvelope.controller.transaction;

import com.smartenvelope.dto.transaction.TransactionRequest;
import com.smartenvelope.dto.transaction.TransactionResponse;
import com.smartenvelope.entity.Transaction;
import com.smartenvelope.entity.User;
import com.smartenvelope.enums.TransactionType;
import com.smartenvelope.exception.BusinessException;
import com.smartenvelope.exception.ResourceAccessDeniedException;
import com.smartenvelope.repository.AccountRepository;
import com.smartenvelope.repository.CategoryRepository;
import com.smartenvelope.repository.EnvelopeRepository;
import com.smartenvelope.repository.TransactionRepository;
import com.smartenvelope.repository.UserRepository;
import com.smartenvelope.service.TransactionService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Map;

/**
 * Controller xử lý các chức năng ghi nhận giao dịch thu, chi và chuyển tiền.
 * Phụ trách bởi: Thành viên 3 (Quang).
 */
@Controller
@RequestMapping("/transactions")
public class TransactionController {

    private final TransactionService transactionService;
    private final TransactionRepository transactionRepository;
    private final EnvelopeRepository envelopeRepository;
    private final AccountRepository accountRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;

    public TransactionController(TransactionService transactionService,
                                 TransactionRepository transactionRepository,
                                 EnvelopeRepository envelopeRepository,
                                 AccountRepository accountRepository,
                                 CategoryRepository categoryRepository,
                                 UserRepository userRepository) {
        this.transactionService = transactionService;
        this.transactionRepository = transactionRepository;
        this.envelopeRepository = envelopeRepository;
        this.accountRepository = accountRepository;
        this.categoryRepository = categoryRepository;
        this.userRepository = userRepository;
    }

    /**
     * Lấy danh sách giao dịch từ TransactionRepository có phân trang Pageable (Mục 4.2).
     */
    @GetMapping
    public String list(@PageableDefault(size = 10, sort = "transactionDate", direction = Sort.Direction.DESC) Pageable pageable,
                       Model model) {
        Page<Transaction> page = transactionRepository.findAll(pageable);
        model.addAttribute("transactions", page.getContent());
        model.addAttribute("page", page);
        model.addAttribute("currentPage", page.getNumber());
        model.addAttribute("totalPages", page.getTotalPages());
        model.addAttribute("totalItems", page.getTotalElements());
        return "transaction/list";
    }

    @GetMapping("/new")
    public String form(Model model) {
        if (!model.containsAttribute("transactionRequest")) {
            model.addAttribute("transactionRequest", new TransactionRequest());
        }
        model.addAttribute("accounts", accountRepository.findAll());
        model.addAttribute("envelopes", envelopeRepository.findAll());
        model.addAttribute("categories", categoryRepository.findAll());
        return "transaction/form";
    }

    @PostMapping
    public String createTransaction(@ModelAttribute("transactionRequest") TransactionRequest request,
                                    RedirectAttributes redirectAttributes,
                                    Model model) {
        try {
            // Lấy người dùng hiện tại (hoặc user đầu tiên trong DB cho môi trường dev/demo)
            User currentUser = userRepository.findAll().stream().findFirst().orElse(null);

            if (request.getType() == TransactionType.EXPENSE) {
                transactionService.recordExpenseWithCover(request, currentUser);
                redirectAttributes.addFlashAttribute("successMessage", "Giao dịch chi tiêu đã được ghi nhận thành công!");
            } else if (request.getType() == TransactionType.INCOME) {
                transactionService.recordIncome(request, currentUser);
                redirectAttributes.addFlashAttribute("successMessage", "Giao dịch thu nhập đã được ghi nhận thành công!");
            } else if (request.getType() == TransactionType.TRANSFER) {
                transactionService.transferEnvelopes(request.getCoverEnvelopeId(), request.getEnvelopeId(), request.getAmount(), currentUser);
                redirectAttributes.addFlashAttribute("successMessage", "Điều chuyển phong bì thành công!");
            }
            return "redirect:/transactions";
        } catch (BusinessException ex) {
            model.addAttribute("errorMessage", ex.getMessage());
            model.addAttribute("transactionRequest", request);
            model.addAttribute("accounts", accountRepository.findAll());
            model.addAttribute("envelopes", envelopeRepository.findAll());
            model.addAttribute("categories", categoryRepository.findAll());
            return "transaction/form";
        }
    }

    /**
     * Endpoint nhận DTO từ TV5 (AI / OCR) - Mục 5.2 trong hướng dẫn.
     * Nhận TransactionRequest bóc tách từ hóa đơn/giọng nói của TV5,
     * gọi qua TransactionService để kiểm tra số dư và bù tiền như bình thường.
     *
     * @param request DTO giao dịch được trích xuất bởi TV5
     * @return DTO TransactionResponse phản hồi kết quả
     */
    @PostMapping("/confirm-draft")
    @ResponseBody
    public ResponseEntity<?> confirmDraft(@Valid @RequestBody TransactionRequest request) {
        try {
            User currentUser = userRepository.findAll().stream().findFirst().orElse(null);
            Transaction transaction = transactionService.recordExpenseWithCover(request, currentUser);
            return ResponseEntity.ok(TransactionResponse.fromEntity(transaction));
        } catch (ResourceAccessDeniedException ex) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", ex.getMessage()));
        } catch (BusinessException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", ex.getMessage()));
        }
    }

    /**
     * Route hủy giao dịch và hoàn tiền (Mục 5.3).
     */
    @PostMapping("/{id}/cancel")
    public String cancelTransaction(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        try {
            transactionService.cancelTransaction(id);
            redirectAttributes.addFlashAttribute("successMessage", "Đã hủy giao dịch và hoàn trả số dư thành công!");
        } catch (BusinessException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/transactions";
    }
}
