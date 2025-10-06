package hamo.job.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.servlet.view.InternalResourceViewResolver;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class WebControllerTest {

    private MockMvc mockMvc;

    @BeforeEach
    void setup() {
        InternalResourceViewResolver viewResolver = new InternalResourceViewResolver();
        viewResolver.setPrefix("/WEB-INF/views/"); // doesn't need to exist on disk
        viewResolver.setSuffix(".jsp");

        mockMvc = MockMvcBuilders
                .standaloneSetup(new WebController())
                .setViewResolvers(viewResolver)
                .build();
    }

    @Test
    @DisplayName("GET / -> redirects to /login")
    void indexRedirectsToLogin() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    @DisplayName("GET /login -> returns login view")
    void loginReturnsLoginView() throws Exception {
        mockMvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andExpect(view().name("login"));
    }

    @Test
    @DisplayName("GET /register -> returns register view")
    void registerReturnsRegisterView() throws Exception {
        mockMvc.perform(get("/register"))
                .andExpect(status().isOk())
                .andExpect(view().name("register"));
    }

    @Test
    @DisplayName("GET /home -> returns home view")
    void homeReturnsHomeView() throws Exception {
        mockMvc.perform(get("/home"))
                .andExpect(status().isOk())
                .andExpect(view().name("home"));
    }

    @Test
    @DisplayName("GET /search without query -> adds empty query to model and returns search view")
    void searchNoQueryAndReturnsSearchViewWithEmptyQuery() throws Exception {
        mockMvc.perform(get("/search"))
                .andExpect(status().isOk())
                .andExpect(view().name("search"))
                .andExpect(model().attribute("query", ""));
    }

    @Test
    @DisplayName("GET /search?q=keyboard -> adds query param to model and returns search view")
    void searchWithQueryAndReturnsSearchView() throws Exception {
        mockMvc.perform(get("/search").param("q", "keyboard"))
                .andExpect(status().isOk())
                .andExpect(view().name("search"))
                .andExpect(model().attribute("query", "keyboard"));
    }

    @Test
    @DisplayName("GET /details?id=5 -> adds productId to model and returns details view")
    void detailsWithIdAndReturnsDetailsView() throws Exception {
        mockMvc.perform(get("/details").param("id", "5"))
                .andExpect(status().isOk())
                .andExpect(view().name("details"))
                .andExpect(model().attribute("productId", "5"));
    }

    @Test
    @DisplayName("GET /details without id -> adds empty productId to model and returns details view")
    void detailsNoIdAndReturnsDetailsViewWithEmptyId() throws Exception {
        mockMvc.perform(get("/details"))
                .andExpect(status().isOk())
                .andExpect(view().name("details"))
                .andExpect(model().attribute("productId", ""));
    }

    @Test
    @DisplayName("GET /shipping -> returns shipping view")
    void shippingAndReturnsShippingView() throws Exception {
        mockMvc.perform(get("/shipping"))
                .andExpect(status().isOk())
                .andExpect(view().name("shipping"));
    }

    @Test
    @DisplayName("GET /order -> returns order view")
    void orderAndReturnsOrderView() throws Exception {
        mockMvc.perform(get("/order"))
                .andExpect(status().isOk())
                .andExpect(view().name("order"));
    }

    @Test
    @DisplayName("GET /account -> returns account view")
    void accountAndReturnsAccountView() throws Exception {
        mockMvc.perform(get("/account"))
                .andExpect(status().isOk())
                .andExpect(view().name("account"));
    }
}
