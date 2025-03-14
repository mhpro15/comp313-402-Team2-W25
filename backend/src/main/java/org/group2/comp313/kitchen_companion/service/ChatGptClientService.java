package org.group2.comp313.kitchen_companion.service;

import org.group2.comp313.kitchen_companion.domain.Category;
import org.group2.comp313.kitchen_companion.domain.CodeBook;
import org.group2.comp313.kitchen_companion.domain.CodeValue;
import org.group2.comp313.kitchen_companion.dto.ai.*;
import org.group2.comp313.kitchen_companion.repository.CategoryRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Optional;

@Service
public class ChatGptClientService extends BaseService{

    private final String COMMA_DELIMITER = ", ";
    private final String NEW_LINE = "\n";

    private final WebClient restClient;
    private final CategoryRepository categoryRepository;
    private final StaticCodeService staticCodeService;

    @Value("${openapi.chatgpt.api-model}")
    private String openApiModel;

    public ChatGptClientService(@Value("${openapi.chatgpt.api-key}") String apiKey, CategoryRepository categoryRepository, StaticCodeService staticCodeService) {

        this.restClient = WebClient.builder()
                .baseUrl("https://api.openai.com/v1")
                .defaultHeader("Authorization", "Bearer " + apiKey)
                .defaultHeader("Accept", "application/json")
                .build();

        this.categoryRepository = categoryRepository;
        this.staticCodeService = staticCodeService;
    }

    /**
     * Retrieves recipe recommendations based on the provided input parameters.
     * The method interacts with an AI-based recommendation system to generate a list of suggested recipes.
     *
     * @param recipeRecommendationRequest the request object containing parameters and preferences for recipe recommendations
     * @return a ChatCompletionResponse containing the recommended recipes or related information
     */
    public ChatCompletionResponse getRecipeRecommendations(AIRecipeRecommendationRequest recipeRecommendationRequest) {

        String recipeRecommendationPrompt = this.buildRecipeRecommendationPrompt(recipeRecommendationRequest);

        ChatCompletionRequest.Message prompt = new ChatCompletionRequest.Message("user", recipeRecommendationPrompt);
        ChatCompletionRequest chatCompletionRequest = new ChatCompletionRequest(openApiModel, List.of(prompt));

        return this.sendPrompt(chatCompletionRequest);
    }

    /**
     * Generates an AI-based meal plan recommendation.
     *
     * <p>This method builds a meal plan recommendation prompt using the provided request by invoking
     * {@code buildMealPlanAIRecommendationPrompt}, wraps the prompt in a {@link ChatCompletionRequest.Message},
     * constructs a {@link ChatCompletionRequest} with the generated message, and finally sends the prompt
     * using {@code sendPrompt}. The resulting AI response is returned as a {@link ChatCompletionResponse} object.
     *
     * @param aiMealPlanRecommendationRequest the request object containing parameters for generating the meal plan recommendation
     * @return a {@link ChatCompletionResponse} containing the AI-generated meal plan recommendation
     */
    public ChatCompletionResponse getMealPlanAIRecommendation(AIMealPlanRecommendationRequest aiMealPlanRecommendationRequest) {

        String mealPlanAIRecommendationPrompt = this.buildMealPlanAIRecommendationPrompt(aiMealPlanRecommendationRequest);

        ChatCompletionRequest.Message prompt = new ChatCompletionRequest.Message("user", mealPlanAIRecommendationPrompt);
        ChatCompletionRequest chatCompletionRequest = new ChatCompletionRequest(openApiModel, List.of(prompt));

        return this.sendPrompt(chatCompletionRequest);
    }

    /**
     * Sends a chat completion request to the remote chat service.
     *
     * <p>This method constructs and sends an HTTP POST request to the "/chat/completions" endpoint using the provided
     * {@code ChatCompletionRequest} object. It then waits for the response, converts it to a {@code ChatCompletionResponse},
     * and returns the response body.</p>
     *
     * <p>If an error occurs during the request (e.g., network issues, invalid response), the method logs the error
     * message and rethrows the exception.</p>
     *
     * @param chatCompletionRequest the request object containing the necessary parameters for generating a chat completion.
     * @return the chat completion response received from the remote service.
     */
    private ChatCompletionResponse sendPrompt(ChatCompletionRequest chatCompletionRequest) {

        try {
            ResponseEntity<ChatCompletionResponse> response = this.restClient.post()
                    .uri("/chat/completions")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(chatCompletionRequest)
                    .retrieve()
                    .toEntity(ChatCompletionResponse.class)
                    .block();

            assert response != null;
            return response.getBody();

        } catch (Exception e) {
            log.error(e.getMessage());
            throw e;
        }
    }

    /**
     * Builds a prompt string to guide an AI-based Meal Plan recommendation system in generating
     * a JSON output for a Meal Plan tailored to the user's input dietary preferences, allergies,
     * and restrictions.
     *
     * @param recipeRecommendationRequest the AIRecipeRecommendationRequest object containing information
     *                                    such as ingredient list, dietary preferences, and allergies.
     * @return a formatted string to be used as a prompt for generating recipe recommendations.
     */
    private String buildRecipeRecommendationPrompt(AIRecipeRecommendationRequest recipeRecommendationRequest) {

        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append("Please create a JSON recipe based on the following schema:");
        stringBuilder.append(NEW_LINE);
        stringBuilder.append(getRecipeDtoJsonAsString());
        stringBuilder.append(NEW_LINE);
        stringBuilder.append(createInstruction("Base the recipe on the following ingredients:", recipeRecommendationRequest.ingredientList(), COMMA_DELIMITER));
        stringBuilder.append(NEW_LINE);
        stringBuilder.append(createInstruction("Please tailor the recipe to the following dietary preferences if any:", recipeRecommendationRequest.mealPreferences(), COMMA_DELIMITER));
        stringBuilder.append(NEW_LINE);
        stringBuilder.append(createInstruction("Here are some allergies and dietary restrictions if any:", recipeRecommendationRequest.allergiesAndRestrictions(), COMMA_DELIMITER));
        stringBuilder.append(NEW_LINE);
        stringBuilder.append(createCategoryListString());
        stringBuilder.append(NEW_LINE);
        stringBuilder.append(createCodeValueListString("Also here are the only values for prepTimeUnitCD and cookTimeUnitCD", StaticCodeService.TIME_UNIT_CODE_BOOK_ID));
        stringBuilder.append(NEW_LINE);
        stringBuilder.append(createInstruction("For the Recipe Image URL please select randomly from the following list: ", List.of(
                "https://ronaldjro.dev/static/img/image1.jpg",
                "https://ronaldjro.dev/static/img/image2.jpg",
                "https://ronaldjro.dev/static/img/image3.jpg",
                "https://ronaldjro.dev/static/img/image4.jpg",
                "https://ronaldjro.dev/static/img/image5.jpg",
                "https://ronaldjro.dev/static/img/image6.jpg",
                "https://ronaldjro.dev/static/img/image7.jpg",
                "https://ronaldjro.dev/static/img/image8.jpg",
                "https://ronaldjro.dev/static/img/image9.jpg",
                "https://ronaldjro.dev/static/img/image10.jpg"
        ), NEW_LINE));
        stringBuilder.append(NEW_LINE);
        stringBuilder.append(createInstruction("Ensure the recipe is complete with: ", List.of(
                "A meaningful title and summary.",
                "Calories and nutritional information filled with realistic values.",
                "Step-by-step instructions grouped logically.",
                "Ingredient groups clearly labeled."
        ), NEW_LINE));
        stringBuilder.append(NEW_LINE);
        stringBuilder.append(NEW_LINE);
        stringBuilder.append("If the ingredient that the user provided were nonsense please set success to false and return the reason why the recipe generation failed");
        stringBuilder.append(NEW_LINE);
        stringBuilder.append("Also Please return a false success if the ingredients list has items from dietary and allergy list and state the reason why the recipe generation failed.");
        stringBuilder.append(NEW_LINE);
        stringBuilder.append("As this is used in an api call please don't include any explanation or any other text just the JSON result. If you include anything the system will break so please don't Thank you.");
        stringBuilder.append(NEW_LINE);
        stringBuilder.append("Can you also please make sure that the JSON result can be deserialized I am getting error with ObjectMapper Thanks.");

        this.log.debug(stringBuilder.toString());

        return stringBuilder.toString();

    }

    /**
     * Builds a prompt string to guide an AI-based Meal Plan recommendation system in generating
     * a JSON output for a Meal Plan tailored to the user's input dietary preferences, allergies,
     * and restrictions.
     *
     * @param aiMealPlanRecommendationRequest object containing information
     *                                    such as  dietary preferences, calorie goals, and allergies.
     * @return a formatted string to be used as a prompt for generating recipe recommendations.
     */
    private String buildMealPlanAIRecommendationPrompt(AIMealPlanRecommendationRequest aiMealPlanRecommendationRequest) {

        StringBuilder stringBuilder = new StringBuilder();
        //String weekWord = aiMealPlanRecommendationRequest.numberOfWeeks() > 1 ? " weeks " : " week ";
        //Integer numberOfMeals = aiMealPlanRecommendationRequest.numberOfWeeks() * 7;

        //stringBuilder.append("Please create a meal plan for ").append(aiMealPlanRecommendationRequest.numberOfWeeks()).append(weekWord).append("using this following instructions:");
        stringBuilder.append("Please generate a 1 week meal plan using the following instructions: ");
        stringBuilder.append(NEW_LINE);
        stringBuilder.append("Can you please generate your response as JSON using the following schema: ");
        stringBuilder.append(NEW_LINE);
        stringBuilder.append(getMealPlanDtoJsonAsString());
        stringBuilder.append(NEW_LINE);
        stringBuilder.append("Can you please provide 3 recipes for each day. So In total I need 21 recipes for the whole Meal Plan week.");
        stringBuilder.append(NEW_LINE);
        stringBuilder.append(createInstruction("Please tailor the recipe for this particular goal: ", List.of(aiMealPlanRecommendationRequest.goalOrPurpose()), COMMA_DELIMITER));
        stringBuilder.append(NEW_LINE);
        stringBuilder.append(createInstruction("Please tailor the recipe to the following dietary preferences if any:", aiMealPlanRecommendationRequest.mealPreferences(), COMMA_DELIMITER));
        stringBuilder.append(NEW_LINE);
        stringBuilder.append(createInstruction("Here are some allergies and dietary restrictions if any:", aiMealPlanRecommendationRequest.allergiesAndRestrictions(), COMMA_DELIMITER));
        stringBuilder.append(NEW_LINE);
        stringBuilder.append(createCategoryListString());
        stringBuilder.append(NEW_LINE);
        stringBuilder.append(createCodeValueListString("Here are the only values for prepTimeUnitCD and cookTimeUnitCD", StaticCodeService.TIME_UNIT_CODE_BOOK_ID));
        stringBuilder.append(NEW_LINE);
        stringBuilder.append(createCodeValueListString("Here are the only values for daysOfWeekCd", StaticCodeService.DAYS_OF_WEEK_CODE_BOOK_ID));
        stringBuilder.append(NEW_LINE);
        stringBuilder.append(createCodeValueListString("Here are the only values for breakfastRecipeSubstituteCd, lunchRecipeSubstituteCd, and dinnerRecipeSubstituteCd", StaticCodeService.NON_RECIPE_SUBSTITUTION_CODE_BOOK_ID));
        stringBuilder.append("You can randomly select a meal plan recipe to use a substitution instead of an actual recipe.");
        stringBuilder.append(NEW_LINE);
        stringBuilder.append(createInstruction("For the Recipe Image URL please select randomly from the following list: ", List.of(
                "https://ronaldjro.dev/static/img/image1.jpg",
                "https://ronaldjro.dev/static/img/image2.jpg",
                "https://ronaldjro.dev/static/img/image3.jpg",
                "https://ronaldjro.dev/static/img/image4.jpg",
                "https://ronaldjro.dev/static/img/image5.jpg",
                "https://ronaldjro.dev/static/img/image6.jpg",
                "https://ronaldjro.dev/static/img/image7.jpg",
                "https://ronaldjro.dev/static/img/image8.jpg",
                "https://ronaldjro.dev/static/img/image9.jpg",
                "https://ronaldjro.dev/static/img/image10.jpg"
        ), NEW_LINE));
        stringBuilder.append(NEW_LINE);
        stringBuilder.append(createInstruction("Ensure the recipe is complete with: ", List.of(
                "A meaningful title and summary.",
                "Calories and nutritional information filled with realistic values.",
                "Step-by-step instructions grouped logically.",
                "Ingredient groups clearly labeled."
        ), NEW_LINE));
        stringBuilder.append(NEW_LINE);
        stringBuilder.append("As this is used in an api call please don't include any explanation or any other text just the JSON result. If you include anything the system will break so please don't Thank you.");
        stringBuilder.append(NEW_LINE);
        stringBuilder.append("Can you also please make sure that the JSON result can be deserialized I am getting error with ObjectMapper Thanks.");
        stringBuilder.append(NEW_LINE);
        stringBuilder.append("Can you also please make sure that you do not include any JSON comments, I am getting error with ObjectMapper Thanks.");

        this.log.info(stringBuilder.toString());

        return stringBuilder.toString();

    }

    private String getRecipeDtoJsonAsString() {
        return "{\n  \"success\": true,\n  \"reasonForFail\": \"string\",\n  \"recipe\": {\n    \"title\": \"string\",\n    \"summary\": \"string\",\n    \"prepTime\": 1073741824,\n    \"prepTimeUnitCd\": 1073741824,\n    \"cookTime\": 1073741824,\n    \"cookTimeUnitCd\": 1073741824,\n    \"servings\": 1073741824,\n    \"yield\": \"string\",\n    \"imageUrl\": \"string\",\n    \"thumbnailUrl\": \"string\",\n    \"calories\": 0,\n    \"carbsG\": 0,\n    \"sugarsG\": 0,\n    \"fatG\": 0,\n    \"categoryIds\": [0],\n    \"ingredientGroups\": [\n      {\n        \"ingredientGroupOrder\": 1073741824,\n        \"label\": \"string\",\n        \"ingredients\": [\n          {\n            \"ingredientOrder\": 1073741824,\n            \"imageUrl\": \"string\",\n            \"label\": \"string\"\n          }\n        ]\n      }\n    ],\n    \"stepGroups\": [\n      {\n        \"stepGroupOrder\": 1073741824,\n        \"label\": \"string\",\n        \"steps\": [\n          {\n            \"stepOrder\": 1073741824,\n            \"label\": \"string\",\n            \"imageUrl\": \"string\"\n          }\n        ]\n      }\n    ]\n  }\n}";
    }

    private String getMealPlanDtoJsonAsString() {
        return "{\n  \"success\": true,\n  \"reasonForFail\": \"string \",\n  \"mealPlanTitle\": \"string\",\n  \"mealPlanDays\": [\n    {\n      \"breakfastRecipe\": {\n        \"title\": \"string \",\n        \"summary\": \"string \",\n        \"prepTime\": 1073741824,\n        \"prepTimeUnitCd\": 1073741824,\n        \"cookTime\": 1073741824,\n        \"cookTimeUnitCd\": 1073741824,\n        \"servings\": 1073741824,\n        \"yield\": \"string \",\n        \"imageUrl\": \"string \",\n        \"thumbnailUrl\": \"string \",\n        \"calories\": 0,\n        \"carbsG\": 0,\n        \"sugarsG\": 0,\n        \"fatG\": 0,\n        \"categoryIds\": [0],\n        \"ingredientGroups\": [\n          {\n            \"ingredientGroupOrder\": 1073741824,\n            \"label\": \"string \",\n            \"ingredients\": [\n              {\n                \"ingredientOrder\": 1073741824,\n                \"imageUrl\": \"string \",\n                \"label\": \"string \"\n              }\n            ]\n          }\n        ],\n        \"stepGroups\": [\n          {\n            \"stepGroupOrder\": 1073741824,\n            \"label\": \"string \",\n            \"steps\": [\n              {\n                \"stepOrder\": 1073741824,\n                \"label\": \"string \",\n                \"imageUrl\": \"string \"\n              }\n            ]\n          }\n        ]\n      },\n      \"lunchRecipe\": {\n        \"title\": \"string \",\n        \"summary\": \"string \",\n        \"prepTime\": 1073741824,\n        \"prepTimeUnitCd\": 1073741824,\n        \"cookTime\": 1073741824,\n        \"cookTimeUnitCd\": 1073741824,\n        \"servings\": 1073741824,\n        \"yield\": \"string \",\n        \"imageUrl\": \"string \",\n        \"thumbnailUrl\": \"string \",\n        \"calories\": 0,\n        \"carbsG\": 0,\n        \"sugarsG\": 0,\n        \"fatG\": 0,\n        \"categoryIds\": [0],\n        \"ingredientGroups\": [\n          {\n            \"ingredientGroupOrder\": 1073741824,\n            \"label\": \"string \",\n            \"ingredients\": [\n              {\n                \"ingredientOrder\": 1073741824,\n                \"imageUrl\": \"string \",\n                \"label\": \"string \"\n              }\n            ]\n          }\n        ],\n        \"stepGroups\": [\n          {\n            \"stepGroupOrder\": 1073741824,\n            \"label\": \"string \",\n            \"steps\": [\n              {\n                \"stepOrder\": 1073741824,\n                \"label\": \"string \",\n                \"imageUrl\": \"string \"\n              }\n            ]\n          }\n        ]\n      },\n      \"dinnerRecipe\": {\n        \"title\": \"string \",\n        \"summary\": \"string \",\n        \"prepTime\": 1073741824,\n        \"prepTimeUnitCd\": 1073741824,\n        \"cookTime\": 1073741824,\n        \"cookTimeUnitCd\": 1073741824,\n        \"servings\": 1073741824,\n        \"yield\": \"string \",\n        \"imageUrl\": \"string \",\n        \"thumbnailUrl\": \"string \",\n        \"calories\": 0,\n        \"carbsG\": 0,\n        \"sugarsG\": 0,\n        \"fatG\": 0,\n        \"categoryIds\": [0],\n        \"ingredientGroups\": [\n          {\n            \"ingredientGroupOrder\": 1073741824,\n            \"label\": \"string \",\n            \"ingredients\": [\n              {\n                \"ingredientOrder\": 1073741824,\n                \"imageUrl\": \"string \",\n                \"label\": \"string \"\n              }\n            ]\n          }\n        ],\n        \"stepGroups\": [\n          {\n            \"stepGroupOrder\": 1073741824,\n            \"label\": \"string \",\n            \"steps\": [\n              {\n                \"stepOrder\": 1073741824,\n                \"label\": \"string \",\n                \"imageUrl\": \"string \"\n              }\n            ]\n          }\n        ]\n      },\n      \"breakfastRecipeSubstituteCd\": 1073741824,\n      \"lunchRecipeSubstituteCd\": 1073741824,\n      \"dinnerRecipeSubstituteCd\": 1073741824,\n      \"daysOfWeekCd\": 1073741824\n    }\n  ]\n}";
        //return "{\n  \"success\": true,\n  \"reasonForFail\": \"string \",\n  \"mealPlanTitle\": \"string\",\n  \"mealPlanWeek\": [\n    {\n      \"mealPlanDay\": [\n        {\n          \"breakfastRecipe\": {\n            \"title\": \"string \",\n            \"summary\": \"string \",\n            \"prepTime\": 1073741824,\n            \"prepTimeUnitCd\": 1073741824,\n            \"cookTime\": 1073741824,\n            \"cookTimeUnitCd\": 1073741824,\n            \"servings\": 1073741824,\n            \"yield\": \"string \",\n            \"imageUrl\": \"string \",\n            \"thumbnailUrl\": \"string \",\n            \"calories\": 0,\n            \"carbsG\": 0,\n            \"sugarsG\": 0,\n            \"fatG\": 0,\n            \"categoryIds\": [0],\n            \"ingredientGroups\": [\n              {\n                \"ingredientGroupOrder\": 1073741824,\n                \"label\": \"string \",\n                \"ingredients\": [\n                  {\n                    \"ingredientOrder\": 1073741824,\n                    \"imageUrl\": \"string \",\n                    \"label\": \"string \"\n                  }\n                ]\n              }\n            ],\n            \"stepGroups\": [\n              {\n                \"stepGroupOrder\": 1073741824,\n                \"label\": \"string \",\n                \"steps\": [\n                  {\n                    \"stepOrder\": 1073741824,\n                    \"label\": \"string \",\n                    \"imageUrl\": \"string \"\n                  }\n                ]\n              }\n            ]\n          },\n          \"lunchRecipe\": {\n            \"title\": \"string \",\n            \"summary\": \"string \",\n            \"prepTime\": 1073741824,\n            \"prepTimeUnitCd\": 1073741824,\n            \"cookTime\": 1073741824,\n            \"cookTimeUnitCd\": 1073741824,\n            \"servings\": 1073741824,\n            \"yield\": \"string \",\n            \"imageUrl\": \"string \",\n            \"thumbnailUrl\": \"string \",\n            \"calories\": 0,\n            \"carbsG\": 0,\n            \"sugarsG\": 0,\n            \"fatG\": 0,\n            \"categoryIds\": [0],\n            \"ingredientGroups\": [\n              {\n                \"ingredientGroupOrder\": 1073741824,\n                \"label\": \"string \",\n                \"ingredients\": [\n                  {\n                    \"ingredientOrder\": 1073741824,\n                    \"imageUrl\": \"string \",\n                    \"label\": \"string \"\n                  }\n                ]\n              }\n            ],\n            \"stepGroups\": [\n              {\n                \"stepGroupOrder\": 1073741824,\n                \"label\": \"string \",\n                \"steps\": [\n                  {\n                    \"stepOrder\": 1073741824,\n                    \"label\": \"string \",\n                    \"imageUrl\": \"string \"\n                  }\n                ]\n              }\n            ]\n          },\n          \"dinnerRecipe\": {\n            \"title\": \"string \",\n            \"summary\": \"string \",\n            \"prepTime\": 1073741824,\n            \"prepTimeUnitCd\": 1073741824,\n            \"cookTime\": 1073741824,\n            \"cookTimeUnitCd\": 1073741824,\n            \"servings\": 1073741824,\n            \"yield\": \"string \",\n            \"imageUrl\": \"string \",\n            \"thumbnailUrl\": \"string \",\n            \"calories\": 0,\n            \"carbsG\": 0,\n            \"sugarsG\": 0,\n            \"fatG\": 0,\n            \"categoryIds\": [0],\n            \"ingredientGroups\": [\n              {\n                \"ingredientGroupOrder\": 1073741824,\n                \"label\": \"string \",\n                \"ingredients\": [\n                  {\n                    \"ingredientOrder\": 1073741824,\n                    \"imageUrl\": \"string \",\n                    \"label\": \"string \"\n                  }\n                ]\n              }\n            ],\n            \"stepGroups\": [\n              {\n                \"stepGroupOrder\": 1073741824,\n                \"label\": \"string \",\n                \"steps\": [\n                  {\n                    \"stepOrder\": 1073741824,\n                    \"label\": \"string \",\n                    \"imageUrl\": \"string \"\n                  }\n                ]\n              }\n            ]\n          },\n          \"breakfastRecipeSubstituteCd\": 1073741824,\n          \"lunchRecipeSubstituteCd\": 1073741824,\n          \"dinnerRecipeSubstituteCd\": 1073741824,\n          \"daysOfWeekCd\": 1073741824\n        }\n      ]\n    }\n  ]\n}";
    }

    /**
     * Constructs an instruction string by combining a leading prompt with a list of items,
     * separated by a specified separator, and formatted with new lines as needed.
     *
     * @param leadingPrompt The initial prompt or heading to prepend to the instruction.
     * @param items The list of items to be included in the instruction, separated by the specified separator.
     * @param separator The string used to separate the items in the generated instruction.
     * @return A constructed instruction string that starts with the leading prompt, followed by the items in the list,
     *         joined by the specified separator. If the items list is null or empty, only the leading prompt is returned.
     */
    private String createInstruction(String leadingPrompt, List<String> items, String separator) {
        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append(leadingPrompt);
        stringBuilder.append(NEW_LINE);

        if(items != null && !items.isEmpty()) {
            for(int i = 0; i < items.size(); i++) {
                stringBuilder.append(items.get(i));
                if(i < items.size() - 1) {
                    stringBuilder.append(separator);
                }
            }
        }

        return stringBuilder.toString();
    }

    /**
     * Creates and returns a formatted string representation of all categories
     * including their IDs and names. The information is fetched from the
     * category repository and each category is appended to the resulting string
     * with a newline after each category's details.
     *
     * @return A formatted string containing details of all categories.
     */
    private String createCategoryListString() {

        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append("Here are the categoryIds and category available:");
        stringBuilder.append(NEW_LINE);

        List<Category> categories = this.categoryRepository.findAll();

        for(Category category : categories) {
            stringBuilder.append(category.toString());
            stringBuilder.append(NEW_LINE);
        }

        return stringBuilder.toString();
    }

    /**
     * Builds and returns a formatted string representation of a code value list for a given codeBookId.
     * Includes an optional leading prompt followed by the list of code values if the codeBook exists.
     *
     * @param leadingPrompt a string to prepend to the generated list, typically used as a label or title
     * @param codeBookId the identifier of the codeBook used to retrieve the list of code values
     * @return a formatted string with the leading prompt and code values, or an empty string if the codeBook is not found
     */
    private String createCodeValueListString(String leadingPrompt, Integer codeBookId) {

        StringBuilder stringBuilder = new StringBuilder();

        Optional<CodeBook> codeBook = this.staticCodeService.getCodeValueListUsingCodeBookID(codeBookId);

        if(codeBook.isPresent()) {
            stringBuilder.append(leadingPrompt);
            stringBuilder.append(NEW_LINE);

            for(CodeValue codeValue : codeBook.get().getCodeValues()) {
                stringBuilder.append(codeValue.toString());
                stringBuilder.append(NEW_LINE);
            }
        }

        return stringBuilder.toString();
    }
}
