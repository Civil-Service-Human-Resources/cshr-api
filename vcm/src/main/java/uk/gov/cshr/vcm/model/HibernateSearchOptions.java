package uk.gov.cshr.vcm.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class HibernateSearchOptions {

    @Builder.Default
    private boolean closed = true;

    @Builder.Default
    private boolean open = true;

    @Builder.Default
    private boolean location = true;

    @Builder.Default
    private boolean salary = true;

    @Builder.Default
    private boolean department = true;


    @Builder.Default
    private boolean contractType = true;

    @Builder.Default
    private boolean workingPatterns = true;

    @Builder.Default
    private boolean active = true;

    @Builder.Default
    private float locationBoost = 01.f;

    @Builder.Default
    private float titleFuzzyBoost = 1.5f;

    @Builder.Default
    private int titleEditDistance = 1;

    @Builder.Default
    private int titlePrefixLength = 1;

    @Builder.Default
    private float titleOriginalBoost = 2f;

    @Builder.Default
    private float titleOriginalPhraseBoost = 2f;

    @Builder.Default
    private float descriptionFuzzyBoost = 1.2f;

    @Builder.Default
    private int descriptionFuzzyPrefix = 1;

    @Builder.Default
    private int descriptionFuzzyEditDistance = 1;

    @Builder.Default
    private boolean titleFuzzyQuery = true;

    @Builder.Default
    private boolean titleQuery = true;

    @Builder.Default
    private boolean wildcardQuery = true;

    @Builder.Default
    private boolean titlePhraseQuery = true;

    @Builder.Default
    private boolean descriptionQuery = true;

    @Builder.Default
    private boolean descriptiopnPhraseQuery = true;    

}
