import React, { useState, useRef, useEffect } from "react";
import {
  Clock,
  Users,
  ChevronDown,
  ChevronUp,
  Scale,
  ArrowRight,
} from "lucide-react";
import { useRouter } from "next/navigation";

const MealPlanRecipe = ({ recipe }) => {
  // State for expand/collapse
  const [expanded, setExpanded] = useState(false);
  const contentRef = useRef(null);
  const router = useRouter();

  // Toggle expand state
  const toggleExpand = () => {
    setExpanded(!expanded);
  };

  // Scroll to the recipe card once expanded
  useEffect(() => {
    if (expanded && contentRef.current) {
      contentRef.current.scrollIntoView({
        behavior: "smooth",
        block: "nearest",
      });
    }
  }, [expanded]);

  return (
    <div
      className={`bg-white rounded-lg shadow-sm border overflow-hidden mb-4 sm:mb-6 transform transition-all duration-300 ${
        expanded ? "shadow-md" : ""
      }`}
      ref={contentRef}
    >
      {/* Recipe image and header with toggle button */}
      <div className="relative">
        <div
          className="overflow-hidden transition-all duration-500 ease-in-out"
          style={{ height: expanded ? "220px" : "100px" }}
        >
          <img
            src={recipe?.imageUrl}
            alt={recipe?.title}
            className="w-full h-full object-cover transition-transform duration-700 ease-out"
            style={{ transform: expanded ? "scale(1.05)" : "scale(1)" }}
          />
        </div>

        {/* Gradient overlay */}
        <div
          className={`absolute inset-0 bg-gradient-to-t ${
            expanded
              ? "from-black/50 to-transparent"
              : "from-black/60 to-transparent"
          } transition-opacity duration-500`}
        ></div>

        {/* Recipe title, toggle button, and categories */}
        <div className="absolute bottom-0 left-0 p-2 sm:p-4 w-full">
          <div className="flex items-start justify-between">
            <h2 className="text-white font-semibold text-lg sm:text-xl tracking-tight leading-tight pr-2 line-clamp-2">
              {recipe.title}
            </h2>
            <button
              onClick={toggleExpand}
              className="p-1 rounded-full bg-white/90 hover:bg-white transition-colors duration-200 flex-shrink-0"
              aria-label={expanded ? "Collapse recipe" : "Expand recipe"}
            >
              {expanded ? (
                <ChevronUp className="h-4 w-4 sm:h-5 sm:w-5" />
              ) : (
                <ChevronDown className="h-4 w-4 sm:h-5 sm:w-5" />
              )}
            </button>
          </div>
          <div className="flex flex-wrap gap-1 mt-1 sm:mt-2 max-h-[28px] min-[400px]:max-h-[60px] overflow-hidden">
            {recipe?.categories?.map((cat, index) => (
              <span
                key={index}
                className="inline-block mb-1 line-clamp-1 min-[400px]:line-clamp-none"
              >
                <span className="text-blue-600 border rounded-full px-2 bg-blue-100 text-xs">
                  {cat.label}
                </span>
              </span>
            ))}
          </div>
        </div>
      </div>

      {/* Expanded content with recipe details */}
      <div
        className={expanded ? "recipe-card-expanded" : "recipe-card-collapsed"}
      >
        <div className="p-3 sm:p-4">
          {/* Recipe summary */}
          <p className="mb-3 sm:mb-4 line-clamp-1 text-sm sm:text-base">
            {recipe.summary}
          </p>

          {/* Recipe details: prep time, cook time, servings, calories */}
          <div className="grid grid-cols-2 sm:flex sm:justify-between gap-2 items-center mb-3 sm:mb-4 text-xs sm:text-sm">
            <div className="flex items-center">
              <Clock className="h-4 w-4 mr-1" />
              <span>{recipe.prepTime} prep</span>
            </div>
            <div className="flex items-center">
              <Clock className="h-4 w-4 mr-1" />
              <span>{recipe.cookTime} cook</span>
            </div>
            <div className="flex items-center">
              <Users className="h-4 w-4 mr-1" />
              <span>Serves {recipe.servings}</span>
            </div>
            <div className="flex items-center">
              <Scale className="h-4 w-4 mr-1" />
              <span>{recipe.calories} cal</span>
            </div>
          </div>

          <div className="recipe-divider"></div>

          {/* Ingredients section */}
          <div>
            <h3 className="font-medium mb-2 text-sm sm:text-base">
              Ingredients
            </h3>
            <ol className="list list-inside text-gray-700 text-sm sm:text-base">
              {recipe?.ingredientGroups
                .sort((a, b) => a.ingredientGroupOrder - b.ingredientGroupOrder)
                .map((group, groupIndex) => (
                  <li key={groupIndex} className="mb-3 sm:mb-4">
                    <h3 className="font-semibold text-base sm:text-lg">
                      {group.label}
                    </h3>
                    <ul className="list-disc list-inside text-gray-700 pl-1 sm:pl-2">
                      {group.ingredients
                        .sort((a, b) => a.ingredientOrder - b.ingredientOrder)
                        .map((ingredient, ingredientIndex) => (
                          <li key={ingredientIndex}>{ingredient.label}</li>
                        ))}
                    </ul>
                  </li>
                ))}
            </ol>
          </div>

          <div className="recipe-divider"></div>

          {/* Instructions section */}
          <div>
            <h3 className="font-medium mb-2 text-sm sm:text-base">
              Instructions
            </h3>
            <ol className="list list-inside text-gray-700 space-y-1 sm:space-y-2 text-sm sm:text-base">
              {recipe?.stepGroups
                .sort((a, b) => a.stepGroupOrder - b.stepGroupOrder)
                .map((group, groupIndex) => (
                  <li key={groupIndex} className="mb-3 sm:mb-4">
                    <h3 className="font-semibold text-base sm:text-lg">
                      {group.label}
                    </h3>
                    <ol className="list-decimal list-inside text-gray-700 pl-1 sm:pl-2">
                      {group.steps
                        .sort((a, b) => a.stepOrder - b.stepOrder)
                        .map((step, stepIndex) => (
                          <li key={stepIndex}>
                            {step.description || step.label}
                          </li>
                        ))}
                    </ol>
                  </li>
                ))}
            </ol>
          </div>
        </div>

        {/* Navigation button to recipe details */}
        <div className="flex justify-end p-3 sm:p-4">
          <div
            onClick={() => router.push(`/recipe/${recipe.id}`)}
            className="cursor-pointer flex border-t justify-around items-center rounded-full w-[150px] hover:bg-gray-100"
          >
            <span>Go to details</span>
            <ArrowRight size={20} />
          </div>
        </div>
      </div>
    </div>
  );
};

export default MealPlanRecipe;
