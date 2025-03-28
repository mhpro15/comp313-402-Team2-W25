import React, { useState } from "react";
import { Edit, SaveIcon, ChefHatIcon, HeartIcon } from "lucide-react";
import Image from "next/image";
import { useRouter } from "next/navigation";
import {
  createRecipe,
  saveRecipe,
  sendRating,
  unsaveRecipe,
} from "@/api/recipe";
import { toast } from "sonner";
import RatingAPILayer from "@/rating_component/react-rating";

const DisplayRecipe = ({
  recipe,
  ratingCurrent,
  updateButton = false,
  saveButton = false,
  categories = [],
}) => {
  const router = useRouter();

  // State hooks
  const [rating, setRating] = useState(ratingCurrent);
  const [saved, setSaved] = useState(recipe.isFavorite);

  // Handler to update rating
  const handleRatingChange = async (e) => {
    const res = await sendRating(recipe.id, e);
    // Check for either status 200 or 201
    if (res?.status === 200 || res?.status === 201) {
      toast("Rating added successfully!");
      setRating({ ...res.data.result, user: rating.user });
    }
  };

  // Handler to save/unsave recipe
  const handleSaveClick = async () => {
    if (saved) {
      const res = await unsaveRecipe(recipe.id);
      if (res?.status === 200) {
        setSaved(!saved);
        toast("Removed from saved recipes");
      }
    } else {
      const res = await saveRecipe(recipe.id);
      if (res?.status === 200 || res?.status === 201) {
        setSaved(!saved);
        toast("Recipe saved successfully");
      }
    }
  };

  // Render content
  return (
    <div>
      <div className="container mx-auto px-4 max-w-4xl">
        {/* Recipe Header */}
        <div className="mb-4 text-center">
          <h1 className="text-4xl font-bold text-gray-800 mb-2">
            {recipe?.title}
          </h1>
          <p className="text-gray-600">{recipe?.summary}</p>
        </div>

        {/* Update Button */}
        {updateButton && (
          <div
            className="flex cursor-pointer w-[200px] text-center p-2 mb-4 font-semibold text-gray-600 border border-gray-200 rounded-lg hover:shadow-md transition-shadow"
            onClick={() => router.push(`/recipe/update/${recipe?.id}`)}
          >
            <Edit size={16} className="mr-2" /> Update this Recipe
          </div>
        )}

        {/* Save Button for new recipes */}
        {saveButton && (
          <div
            className="flex cursor-pointer w-[200px] text-center p-2 mb-4 font-semibold text-gray-600 border border-gray-200 rounded-lg hover:shadow-md transition-shadow"
            onClick={async () => {
              const res = await createRecipe(recipe);
              if (res?.status === 200 || res?.status === 201) {
                toast("Recipe added successfully!");
                setTimeout(() => {
                  router.replace(`/recipe/${res.data.result.id}`);
                }, 2500);
              }
            }}
          >
            <SaveIcon size={16} className="mr-2" /> Save this Recipe
          </div>
        )}

        {/* Recipe Image & Save Icon */}
        <div className="relative w-full h-[300px] md:h-[400px] rounded-lg overflow-hidden mb-8">
          {!saveButton && !updateButton && rating?.user && (
            <div className="absolute top-5 right-5 z-10">
              <HeartIcon
                size={25}
                className={`${
                  saved && "fill-red-500"
                } text-red-500 hover:scale-110`}
                onClick={handleSaveClick}
              />
            </div>
          )}
          <Image
            unoptimized
            src={
              (recipe?.imageUrl !== "x" && recipe?.imageUrl) ||
              "https://www.themealdb.com/images/media/meals/58oia61564916529.jpg"
            }
            alt={recipe?.title || "image"}
            layout="fill"
            objectFit="cover"
            className="rounded-lg"
            priority
          />
        </div>

        {/* Recipe Categories */}
        {saveButton ? (
          <div className="flex flex-wrap gap-2 mb-2">
            {recipe?.categoryIds?.map((catId, index) => {
              const category = categories?.data.find((cat) => cat.id === catId);
              if (!category) return null;
              return (
                <div
                  key={index}
                  className="flex items-center gap-1 px-3 py-1 bg-blue-100 text-blue-800 rounded-full cursor-pointer"
                  // Note: setFormData is not defined in this component
                  onClick={() =>
                    setFormData((prev) => ({
                      ...prev,
                      categoryIds: prev.categoryIds.filter(
                        (id) => id !== catId
                      ),
                    }))
                  }
                >
                  <span>{category.label}</span>
                </div>
              );
            })}
          </div>
        ) : (
          <div className="flex">
            <div className="flex flex-wrap gap-2 mb-2 w-2/3">
              {recipe.categories
                ?.sort((a, b) => a.id - b.id)
                .map((cat) => (
                  <div
                    key={cat.id}
                    className="flex items-center gap-1 px-3 py-1 bg-green-200 text-gray-800 rounded-full cursor-pointer"
                  >
                    <span>{cat.label}</span>
                  </div>
                ))}
            </div>
            {/* Rating Section */}
            {rating && (
              <div className="flex flex-col ml-auto w-1/3 items-end">
                <div className="flex">
                  <RatingAPILayer
                    initialRating={rating?.ratingValue}
                    onChange={handleRatingChange}
                    emptySymbol={[
                      <ChefHatIcon className="text-gray-300 font-semibold" />,
                      <ChefHatIcon className="text-gray-300 font-semibold" />,
                      <ChefHatIcon className="text-gray-300 font-semibold" />,
                      <ChefHatIcon className="text-gray-300 font-semibold" />,
                      <ChefHatIcon className="text-gray-300 font-semibold" />,
                    ]}
                    fullSymbol={[
                      <ChefHatIcon className="text-gray-600 font-semibold" />,
                      <ChefHatIcon className="text-red-600 font-semibold" />,
                      <ChefHatIcon className="text-orange-400 font-semibold" />,
                      <ChefHatIcon className="text-yellow-300 font-semibold" />,
                      <ChefHatIcon className="text-green-600 font-semibold" />,
                    ]}
                    fractions={4}
                    readonly={
                      updateButton ||
                      rating.currentUserRating ||
                      rating.user === null
                    }
                  />
                  <p className="font-semibold text-lg text-right">
                    {rating?.ratingValue} ({rating?.numberOfRatings})
                  </p>
                </div>
                {!updateButton && rating?.currentUserRating && (
                  <p className="text-sm text-gray-600 text-right">
                    Your rating: {rating?.currentUserRating}
                  </p>
                )}
              </div>
            )}
          </div>
        )}

        {/* Recipe Meta Information */}
        <div className="my-4 border-b border-gray-200">
          <div className="flex justify-between text-sm text-gray-600">
            <p className="flex items-center">
              <span className="font-medium mr-2">Author:</span>{" "}
              {recipe?.createdBy}
            </p>
            <p className="flex items-center">
              <span className="font-medium mr-2">Created on:</span>
              {new Date(recipe?.createdAt || Date.now()).toLocaleDateString()}
            </p>
          </div>
        </div>

        {/* Recipe Details */}
        <div className="bg-white p-6 rounded-lg shadow-md">
          {/* Ingredients Section */}
          <div className="mb-6">
            <h2 className="text-2xl font-semibold text-gray-800 mb-3">
              Ingredients
            </h2>
            <ol className="list list-inside text-gray-700">
              {recipe?.ingredientGroups
                .sort((a, b) => a.ingredientGroupOrder - b.ingredientGroupOrder)
                .map((ingredientGroup, index) => (
                  <li key={index} className="mb-4">
                    <h3 className="font-semibold text-lg">
                      {ingredientGroup.label}
                    </h3>
                    <ul className="list-disc list-inside text-gray-700">
                      {ingredientGroup.ingredients
                        .sort((a, b) => a.ingredientOrder - b.ingredientOrder)
                        .map((ingredient, idx) => (
                          <li key={idx}>{ingredient.label}</li>
                        ))}
                    </ul>
                  </li>
                ))}
            </ol>
          </div>

          {/* Instructions Section */}
          <div>
            <h2 className="text-2xl font-semibold text-gray-800 mb-3">
              Instructions
            </h2>
            <ol className="list list-inside text-gray-700 space-y-2">
              {recipe?.stepGroups
                .sort((a, b) => a.stepGroupOrder - b.stepGroupOrder)
                .map((stepGroup, index) => (
                  <li key={index} className="mb-4">
                    <h3 className="font-semibold text-lg">{stepGroup.label}</h3>
                    <ol className="list-decimal list-inside text-gray-700">
                      {stepGroup.steps
                        .sort((a, b) => a.stepOrder - b.stepOrder)
                        .map((step, idx) => (
                          <li key={idx}>{step.description || step.label}</li>
                        ))}
                    </ol>
                  </li>
                ))}
            </ol>
          </div>
        </div>

        {/* Recipe Information */}
        <div className="bg-white p-8 rounded-xl shadow-lg my-8">
          <h2 className="text-2xl font-bold text-gray-800 mb-4">
            Recipe Information
          </h2>
          <div className="grid grid-cols-2 md:grid-cols-4 gap-6 mb-8">
            <div className="text-center p-4 border rounded-lg hover:shadow-md transition-shadow">
              <p className="text-gray-600 text-sm uppercase tracking-wide">
                Prep Time
              </p>
              <p className="font-semibold text-lg mt-1">
                {recipe?.prepTime} {recipe?.prepTimeUnitCd.label}
              </p>
            </div>
            <div className="text-center p-4 border rounded-lg hover:shadow-md transition-shadow">
              <p className="text-gray-600 text-sm uppercase tracking-wide">
                Cook Time
              </p>
              <p className="font-semibold text-lg mt-1">
                {recipe?.cookTime} {recipe?.cookTimeUnitCd.label}
              </p>
            </div>
            <div className="text-center p-4 border rounded-lg hover:shadow-md transition-shadow">
              <p className="text-gray-600 text-sm uppercase tracking-wide">
                Servings
              </p>
              <p className="font-semibold text-lg mt-1">{recipe?.servings}</p>
            </div>
            <div className="text-center p-4 border rounded-lg hover:shadow-md transition-shadow">
              <p className="text-gray-600 text-sm uppercase tracking-wide">
                Yield
              </p>
              <p className="font-semibold text-lg mt-1">{recipe?.yield}</p>
            </div>
          </div>
        </div>

        {/* Nutrition Facts */}
        <div className="bg-white p-8 rounded-xl shadow-lg my-8">
          <h2 className="text-2xl font-bold text-gray-800 mb-4">
            Nutrition Facts
          </h2>
          <div className="grid grid-cols-2 md:grid-cols-4 gap-6">
            <div className="text-center p-4 border bg-white rounded-lg shadow-sm hover:shadow-md transition-shadow">
              <p className="text-gray-600 text-sm uppercase tracking-wide">
                Calories
              </p>
              <p className="font-semibold text-lg mt-1">{recipe?.calories}</p>
            </div>
            <div className="text-center p-4 border bg-white rounded-lg shadow-sm hover:shadow-md transition-shadow">
              <p className="text-gray-600 text-sm uppercase tracking-wide">
                Carbs
              </p>
              <p className="font-semibold text-lg mt-1">{recipe?.carbsG}g</p>
            </div>
            <div className="text-center p-4 border bg-white rounded-lg shadow-sm hover:shadow-md transition-shadow">
              <p className="text-gray-600 text-sm uppercase tracking-wide">
                Sugars
              </p>
              <p className="font-semibold text-lg mt-1">{recipe?.sugarsG}g</p>
            </div>
            <div className="text-center p-4 border bg-white rounded-lg shadow-sm hover:shadow-md transition-shadow">
              <p className="text-gray-600 text-sm uppercase tracking-wide">
                Fat
              </p>
              <p className="font-semibold text-lg mt-1">{recipe?.fatG}g</p>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default DisplayRecipe;
