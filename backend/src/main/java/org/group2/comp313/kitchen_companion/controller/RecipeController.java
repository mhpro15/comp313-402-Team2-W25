package org.group2.comp313.kitchen_companion.controller;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.apache.camel.ProducerTemplate;
import org.group2.comp313.kitchen_companion.domain.Recipe;
import org.group2.comp313.kitchen_companion.dto.UserInteractionDto;
import org.group2.comp313.kitchen_companion.dto.ai.AIRecipeRecommendationResult;
import org.group2.comp313.kitchen_companion.dto.ai.AIRecipeRecommendationRequest;
import org.group2.comp313.kitchen_companion.dto.ApiResult;
import org.group2.comp313.kitchen_companion.dto.recipe.RecipeComponentUpdateDto;
import org.group2.comp313.kitchen_companion.dto.recipe.RecipeDto;
import org.group2.comp313.kitchen_companion.dto.recipe.RecipeSummaryForCards;
import org.group2.comp313.kitchen_companion.dto.recipe.SaveRecipeDto;
import org.group2.comp313.kitchen_companion.service.AWSS3Service;
import org.group2.comp313.kitchen_companion.service.IngredientGroupService;
import org.group2.comp313.kitchen_companion.service.RecipeService;

import org.group2.comp313.kitchen_companion.service.StepGroupService;
import org.group2.comp313.kitchen_companion.utility.EntityToBeUpdatedNotFoundException;
import org.group2.comp313.kitchen_companion.utility.ValidationGroups;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/recipe")
@SecurityRequirement(name = "Keycloak")
@Tag(name = "Recipe API")
public class RecipeController extends BaseController {

    private final RecipeService recipeService;
    private final AWSS3Service awss3Service;
    private final IngredientGroupService ingredientGroupService;
    private final StepGroupService stepGroupService;
    private final ProducerTemplate camelTemplate;

    public RecipeController(RecipeService recipeService, AWSS3Service awss3Service, IngredientGroupService ingredientGroupService, StepGroupService stepGroupService, ProducerTemplate camelTemplate) {
        this.recipeService = recipeService;
        this.awss3Service = awss3Service;
        this.ingredientGroupService = ingredientGroupService;
        this.stepGroupService = stepGroupService;
        this.camelTemplate = camelTemplate;
    }

    @GetMapping("/my-recipe")
    public ResponseEntity<ApiResult<Page<RecipeSummaryForCards>>>getAllUserRecipes(@RequestParam Integer page,
                                                                                   @RequestParam Integer size,
                                                                                   @AuthenticationPrincipal(expression = "claims['email']") String userEmail) {

        log.info("Request to retrieve my recipe for {}", userEmail);

        try {
            return ResponseEntity.ok(new ApiResult<>("" ,this.recipeService.getRecipesByCreatedBy(userEmail, page, size)));
        } catch (Exception e) {
            log.error(e.getLocalizedMessage());
            return new ResponseEntity<>(new ApiResult<>(e.getLocalizedMessage(), null), HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }

    @PostMapping
    public ResponseEntity<ApiResult<Recipe>> createRecipe(@NotNull @RequestBody @Validated(ValidationGroups.Create.class) RecipeDto createRecipeDto,
                                                          @AuthenticationPrincipal(expression = "claims['email']") String createdByEmail) {

        log.info("Request to create recipe: {}", createRecipeDto.toString());
        log.info("Request By {}", createdByEmail);

        try {
            Recipe newRecipe = this.recipeService.createRecipe(createRecipeDto, createdByEmail);

            if(newRecipe == null) {
                return new ResponseEntity<>(new ApiResult<>("Recipe creation failed.", null), HttpStatus.INTERNAL_SERVER_ERROR);
            }

            return new ResponseEntity<>(new ApiResult<>("New recipe had been successfully created.", newRecipe),HttpStatus.OK);

        } catch (Exception e) {
            return new ResponseEntity<>(new ApiResult<>(e.getLocalizedMessage(), null), HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }

    @PostMapping(path = "/img-upload", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public String uploadFile(@RequestParam("file") MultipartFile file, @AuthenticationPrincipal(expression = "claims['email']") String createdByEmail) {

        log.info("Request to upload file: {}", file.getOriginalFilename());
        log.info("Request to upload file size: {}", file.getSize());

        if(createdByEmail.equalsIgnoreCase("user1@mail.com") || createdByEmail.equalsIgnoreCase("user2@mail.com")) {
            return awss3Service.uploadFile(file.getOriginalFilename(), file);
        } else {
            return "https://ronaldjro.dev/static/img/image5.jpg";
        }
    }

    @PostMapping("/ai-recipe-recommend")
    public ResponseEntity<ApiResult<AIRecipeRecommendationResult>> getAIRecipeRecommendation(@RequestBody @Valid() @NotNull AIRecipeRecommendationRequest request) {

        log.debug("Request to get ai recipe recommendation: {}", request);

        try {
            AIRecipeRecommendationResult result = this.recipeService.getAiRecipeRecommendation(request);
            return new ResponseEntity<>(new ApiResult<>("Successful Generation.", result), HttpStatus.OK);
        } catch (Exception e) {
            log.error(e.getLocalizedMessage(), e);
            return new ResponseEntity<>(new ApiResult<>(e.getLocalizedMessage(), null), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResult<Boolean>> updateRecipe(@PathVariable Integer id,
                                                           @NotNull @RequestBody @Validated(ValidationGroups.Update.class) RecipeDto updateRecipeDto,
                                                           @AuthenticationPrincipal(expression = "claims['email']") String createdByEmail) {

        log.info("Request to update recipe: {}", updateRecipeDto.toString());

        try {
            Boolean result = this.recipeService.updateRecipe(id, updateRecipeDto, createdByEmail);
            return new ResponseEntity<>(new ApiResult<>("Recipe Update Successful.", result), HttpStatus.OK);
        } catch (EntityToBeUpdatedNotFoundException e) {
            log.error(e.getLocalizedMessage());
            return new ResponseEntity<>(new ApiResult<>(e.getLocalizedMessage(), null), HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            log.error(e.getLocalizedMessage());
            return new ResponseEntity<>(new ApiResult<>(e.getLocalizedMessage(), false), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    @PatchMapping("{recipeId}/step-group/{stepGroupId}")
    public ResponseEntity<ApiResult<Boolean>> updateStepGroup(@PathVariable Integer recipeId,
                                                              @PathVariable Integer stepGroupId,
                                                              @NotNull @Valid @RequestBody RecipeComponentUpdateDto recipeComponentUpdateDto,
                                                              @AuthenticationPrincipal(expression = "claims['email']") String createdByEmail) {

        log.info("Request to update step group: {}", recipeComponentUpdateDto.toString());

        try {
            return updateRecipeComponent(recipeId, stepGroupId, recipeComponentUpdateDto, createdByEmail, true);
        } catch (Exception e) {
            return new ResponseEntity<>(new ApiResult<>(e.getLocalizedMessage(), false), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PatchMapping("{recipeId}/ingredient-group/{ingredientGroupId}")
    public ResponseEntity<ApiResult<Boolean>> updateIngredientGroup(@PathVariable Integer recipeId,
                                                                    @PathVariable Integer ingredientGroupId,
                                                                    @NotNull @Valid @RequestBody RecipeComponentUpdateDto recipeComponentUpdateDto,
                                                                    @AuthenticationPrincipal(expression = "claims['email']") String createdByEmail) {

        log.info("Request to update recipe group: {}", recipeComponentUpdateDto.toString());

        return updateRecipeComponent(recipeId, ingredientGroupId, recipeComponentUpdateDto, createdByEmail, false);

    }

    @PostMapping("/save")
    public ResponseEntity<ApiResult<Void>> saveRecipeForUser(@Valid @RequestBody SaveRecipeDto saveRecipeDto,
                                                             @AuthenticationPrincipal(expression = "claims['email']") String createdByEmail,
                                                             @RequestHeader(value = "Session-Id", required = false) String sessionId) {

        if(sessionId != null) {
            UserInteractionDto userInteractionDto = new UserInteractionDto(sessionId, saveRecipeDto.recipeId(), "saved");
            this.camelTemplate.asyncSendBody("direct:userInteractionEvents", userInteractionDto);
        }

        try {
            this.recipeService.saveRecipeForUser(saveRecipeDto.recipeId(), createdByEmail);
            return new ResponseEntity<>(new ApiResult<>("Recipe Saved Successfully.", null), HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(new ApiResult<>(e.getLocalizedMessage(), null), HttpStatus.BAD_REQUEST);
        }
    }

    @DeleteMapping("/save/{recipeId}")
    public ResponseEntity<ApiResult<Void>> removeSavedRecipeForUser(@PathVariable(name = "recipeId") Integer id,
                                                                    @AuthenticationPrincipal(expression = "claims['email']") String createdByEmail) {
        this.recipeService.removeSavedRecipe(id, createdByEmail);
        return new ResponseEntity<>(new ApiResult<>("Saved Recipe Removed.", null), HttpStatus.OK);
    }

    @GetMapping("/saved")
    public ResponseEntity<ApiResult<Page<RecipeSummaryForCards>>> getSavedRecipes(@RequestParam Integer page,
                                                                                  @RequestParam Integer size,
                                                                                  @AuthenticationPrincipal(expression = "claims['email']") String email) {
        try {
            Page<RecipeSummaryForCards> savedRecipeForUser = this.recipeService.getSavedRecipeForUser(page, size, email);
            return ResponseEntity.ok(new ApiResult<>(null, savedRecipeForUser));
        } catch (Exception e) {
            log.error(e.getLocalizedMessage());
            return new ResponseEntity<>(new ApiResult<>(e.getLocalizedMessage(), null), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private ResponseEntity<ApiResult<Boolean>> updateRecipeComponent(Integer recipeId,
                                                                     Integer groupId,
                                                                     RecipeComponentUpdateDto recipeComponentUpdateDto,
                                                                     String createdByEmail,
                                                                     Boolean forStepGroup) {
        try {
            if (forStepGroup) {
                this.stepGroupService.updateStepGroup(recipeComponentUpdateDto, recipeId,  groupId, createdByEmail);
            } else {
                this.ingredientGroupService.updateIngredientGroup(recipeComponentUpdateDto, recipeId,  groupId, createdByEmail);
            }
            return new ResponseEntity<>(new ApiResult<>("Recipe Component Update Successful.", true), HttpStatus.OK);
        } catch (EntityToBeUpdatedNotFoundException e) {
            log.error(e.getLocalizedMessage());
            return new ResponseEntity<>(new ApiResult<>(e.getLocalizedMessage(), false), HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            log.error(e.getLocalizedMessage());
            return new ResponseEntity<>(new ApiResult<>(e.getLocalizedMessage(), false), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
