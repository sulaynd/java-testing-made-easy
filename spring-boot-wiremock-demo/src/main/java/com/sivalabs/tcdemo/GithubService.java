package com.sivalabs.tcdemo;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.extern.slf4j.Slf4j;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@Slf4j
public class GithubService {

    private final String githubApiBaseUrl;

    public GithubService(@Value("${github.api.base-url}") String githubApiBaseUrl) {
        this.githubApiBaseUrl = githubApiBaseUrl;
    }

    @CircuitBreaker(name = "github-service")
    @Retry(name = "github-service", fallbackMethod = "getGitHubUserFallback")
    public GitHubUser getGithubUserProfile(String username) {
        try {
            log.info("Github API BaseUrl:" + githubApiBaseUrl);
            RestTemplate restTemplate = new RestTemplate();
            return restTemplate.getForObject(githubApiBaseUrl + "/users/" + username, GitHubUser.class);
        } catch (RuntimeException e) {
            log.error("Fail to fetch github profile", e);
            throw new GitHubServiceException("Fail to fetch github profile for " + username);
        }
    }

    Optional<GitHubUser> getGitHubUserFallback(String username, Throwable t) {
        log.info("github-service get userName fallback: username:{}, Error: {} ", username, t.getMessage());
        return Optional.empty();
    }

}
