package com.example.final_project.src.funding.repository;

import com.example.final_project.src.funding.entity.Funding;
import com.example.final_project.src.funding.entity.FundingCategory;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FundingCategoryRepository extends JpaRepository<FundingCategory, Long> {

    // 모든 펀딩 카테고리 출력
    @NotNull List<FundingCategory> findAll();

    // 카테고리 Id로 카테고리 객체 찾기
    Optional<FundingCategory> findFundingCategoriesById(Long Id);

    // 카테고리 네임과 완전 일치하는 카테고리 출력
    Optional<FundingCategory> findFundingCategoryByCategoryNameEquals(String category);

    // 특정 키워드 단어가 카테고리 이름에 포함하는 카테고리 리스트 출력
    List<FundingCategory> findFundingCategoriesByCategoryNameContaining(String keyword);

    // 특정 단어가 카테고리 이름에 포함하는 카테고리 Id 리스트 출력
    List<Long> findByCategoryNameContaining(String keyword);
}
