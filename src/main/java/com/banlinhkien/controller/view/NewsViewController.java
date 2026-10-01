package com.banlinhkien.controller.view;

import com.banlinhkien.entity.NewsArticle;
import com.banlinhkien.service.NewsService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class NewsViewController {

    private final NewsService newsService;

    @GetMapping({"/tin-tuc", "/news"})
    public String index(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "6") int size,
            @RequestParam(required = false) String q,
            Model model
    ) {
        Page<NewsArticle> newsPage = newsService.getNewsPage(page, size, q);
        List<NewsArticle> latestNews = newsService.getLatestNews(5);

        model.addAttribute("newsPage", newsPage);
        model.addAttribute("latestNews", latestNews);
        model.addAttribute("keyword", q);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", newsPage.getTotalPages());

        return "news/index";
    }

    @GetMapping({"/tin-tuc/{slugOrId}", "/news/{slugOrId}"})
    public String detail(@PathVariable String slugOrId, Model model) {
        NewsArticle article = newsService.getNewsByIdOrSlug(slugOrId)
                .orElse(null);

        if (article == null) {
            return "redirect:/tin-tuc";
        }

        List<NewsArticle> relatedNews = newsService.getLatestNews(4);
        model.addAttribute("article", article);
        model.addAttribute("relatedNews", relatedNews);

        return "news/detail";
    }
}
