package com.p2pexchange.controller;

import com.p2pexchange.model.File;
import com.p2pexchange.service.FileService;
import com.p2pexchange.service.CryptoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/files")
public class FileController {

    private final FileService fileService;
    private final CryptoService cryptoService;
    
    @Autowired
    public FileController(FileService fileService, CryptoService cryptoService) {
        this.fileService = fileService;
        this.cryptoService = cryptoService;
    }
    
    @GetMapping
    public String listFiles(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String userId = auth.getName();
        
        List<File> files = fileService.getFilesByUser(userId);
        model.addAttribute("files", files);
        
        return "files/list";
    }
    
    @GetMapping("/{fileId}")
    public String viewFile(@PathVariable String fileId, Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String userId = auth.getName();
        
        File file = fileService.getFileById(fileId);
        
        // Check if user has access to this file
        if (!fileService.hasAccess(fileId, userId)) {
            return "redirect:/files?error=access";
        }
        
        model.addAttribute("file", file);
        return "files/view";
    }
    
    @GetMapping("/{fileId}/download")
    public ResponseEntity<Resource> downloadFile(@PathVariable String fileId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String userId = auth.getName();
        
        // Check if user has access to this file
        if (!fileService.hasAccess(fileId, userId)) {
            return ResponseEntity.status(403).build();
        }
        
        File file = fileService.getFileById(fileId);
        Resource resource = fileService.loadFileAsResource(fileId);
        
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getFilename() + "\"")
                .body(resource);
    }
    
    @GetMapping("/upload")
    public String showUploadForm(Model model) {
        return "files/upload";
    }
    
    @PostMapping("/upload")
    public String handleFileUpload(@RequestParam("file") MultipartFile file,
                                  @RequestParam(value = "encryptionType", defaultValue = "AES") String encryptionType,
                                  RedirectAttributes redirectAttributes) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String userId = auth.getName();
        
        try {
            File savedFile = fileService.storeFile(file, userId, encryptionType);
            redirectAttributes.addFlashAttribute("message", 
                    "You successfully uploaded " + file.getOriginalFilename() + "!");
            
            return "redirect:/files";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", 
                    "Could not upload " + file.getOriginalFilename() + ": " + e.getMessage());
            
            return "redirect:/files/upload";
        }
    }
    
    @GetMapping("/{fileId}/delete")
    public String deleteFile(@PathVariable String fileId, RedirectAttributes redirectAttributes) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String userId = auth.getName();
        
        // Check if user owns this file
        File file = fileService.getFileById(fileId);
        if (!file.getOwner().equals(userId)) {
            redirectAttributes.addFlashAttribute("error", "You do not have permission to delete this file.");
            return "redirect:/files";
        }
        
        boolean deleted = fileService.deleteFile(fileId);
        if (deleted) {
            redirectAttributes.addFlashAttribute("message", "File deleted successfully.");
        } else {
            redirectAttributes.addFlashAttribute("error", "Failed to delete file.");
        }
        
        return "redirect:/files";
    }
    
    @GetMapping("/search")
    public String searchFiles(@RequestParam("query") String query, Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String userId = auth.getName();
        
        List<File> files = fileService.searchFiles(query, userId);
        model.addAttribute("files", files);
        model.addAttribute("query", query);
        
        return "files/list";
    }
}