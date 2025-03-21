import React, { useState, useEffect } from "react";
import { BarLoader } from "react-spinners";
import RecipeCard from "@/components/RecipeCard";
import { Circle, Grid, List, RefreshCwIcon } from "lucide-react";
import RecipeRow from "./RecipeRow";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "./ui/select";
import { redirect } from "next/navigation";

const RecipesResult = ({
  isSearching = false,
  isLoading = false,
  recipeCardData = [],
  displayType = "grid",
  sort = "default",
  version = 1,
  searchKey = "",
  user = {},
  selectedCategory = [],
}) => {
  // State for the current view mode (grid or list)
  const [viewMode, setViewMode] = useState(displayType);

  // Restore view mode from session storage if available
  useEffect(() => {
    const lastView = sessionStorage.getItem("view");
    setViewMode(lastView || displayType);
  }, [displayType]);

  // Change page routing based on sort option selected
  const handleSortChange = (selectedSort) => {
    setTimeout(() => {
      // Construct query string with the selected sort option and other parameters
      let category = selectedCategory[0] || "";
      switch (selectedSort) {
        case "title-asc":
          redirect(
            `/recipes?sort=title-asc&search=${searchKey}&category=${category}`
          );
          break;
        case "title-desc":
          redirect(
            `/recipes?sort=title-desc&search=${searchKey}&category=${category}`
          );
          break;
        case "rating-asc":
          redirect(
            `/recipes?sort=rating-asc&search=${searchKey}&category=${category}`
          );
          break;
        case "rating-desc":
          redirect(
            `/recipes?sort=rating-desc&search=${searchKey}&category=${category}`
          );
          break;
        default:
          redirect(`/recipes`);
          break;
      }
    }, 10);
  };

  return (
    <div className="w-full max-w-[1600px] mx-auto">
      {/* Section header and controls for version 1 */}
      {version === 1 && (
        <>
          <h2 className="font-bold text-2xl text-center mt-10">All Recipes</h2>

          {/* No recipes found message */}
          {recipeCardData.length === 0 && !isLoading ? (
            <div className="mx-auto text-center mt-20">
              <p className="mb-10">No recipes found...</p>
            </div>
          ) : (
            <div className="flex flex-col min-[450px]:flex-row justify-between items-center my-2 w-[90%] mx-auto">
              {/* Sorting dropdown */}
              <div className="w-[80%] my-4 flex justify-end min-[450px]:justify-start">
                <Select
                  value={sort}
                  onValueChange={handleSortChange}
                  closeOnSelect
                >
                  <SelectTrigger className="w-[200px]">
                    <SelectValue placeholder="Sort by..." />
                  </SelectTrigger>
                  <SelectContent>
                    <SelectItem value="default">
                      <span className="flex items-center">
                        <Circle className="mr-2 h-2 w-2 fill-current" />
                        Default Order
                      </span>
                    </SelectItem>
                    <SelectItem value="title-asc">
                      <span className="flex items-center">
                        <Circle className="mr-2 h-2 w-2 fill-current" />
                        Title (A-Z)
                      </span>
                    </SelectItem>
                    <SelectItem value="title-desc">
                      <span className="flex items-center">
                        <Circle className="mr-2 h-2 w-2 fill-current" />
                        Title (Z-A)
                      </span>
                    </SelectItem>
                    <SelectItem value="rating-asc">
                      <span className="flex items-center">
                        <Circle className="mr-2 h-2 w-2 fill-current" />
                        Rating (Low to High)
                      </span>
                    </SelectItem>
                    <SelectItem value="rating-desc">
                      <span className="flex items-center">
                        <Circle className="mr-2 h-2 w-2 fill-current" />
                        Rating (High to Low)
                      </span>
                    </SelectItem>
                  </SelectContent>
                </Select>
                {/* Refresh button */}
                <button
                  className="px-2 py-2 md:px-4 border rounded ml-2"
                  onClick={() => window.location.assign("/recipes?page=1")}
                >
                  <RefreshCwIcon size={20} />
                </button>
              </div>

              {/* View mode toggle buttons */}
              <div className="w-[80%] flex justify-end">
                <button
                  className={`px-2 py-2 md:px-4 border rounded mr-2 ${
                    viewMode !== "list"
                      ? "bg-gray-500 text-white"
                      : "bg-white text-gray-500"
                  }`}
                  onClick={() => {
                    setViewMode("grid");
                    sessionStorage.setItem("view", "grid");
                  }}
                >
                  <Grid size={20} />
                </button>
                <button
                  className={`px-2 py-2 md:px-4 border rounded ${
                    viewMode === "list"
                      ? "bg-gray-500 text-white"
                      : "bg-white text-gray-500"
                  }`}
                  onClick={() => {
                    setViewMode("list");
                    sessionStorage.setItem("view", "list");
                  }}
                >
                  <List size={20} />
                </button>
              </div>
            </div>
          )}
        </>
      )}

      {/* Recipes display */}
      {!isSearching && !isLoading ? (
        <>
          {viewMode === "list" && version === 1 ? (
            <div className="mx-auto w-[90%]">
              {recipeCardData.map((recipe, index) => (
                <RecipeRow key={index} recipe={recipe} user={user} />
              ))}
            </div>
          ) : (
            <div
              className={`grid grid-cols-1 ${
                version === 2
                  ? "md:grid-cols-2 lg:grid-cols-3 2xl:grid-cols-4"
                  : "md:grid-cols-2 min-[1250px]:grid-cols-3 2xl:grid-cols-3 min-[1650px]:grid-cols-4"
              } gap-6 xl:px-4 py-4 mx-auto max-w-[1600px]`}
            >
              {recipeCardData.map((recipe, index) => (
                <RecipeCard
                  key={index}
                  data={recipe}
                  version={version}
                  user={user}
                />
              ))}
            </div>
          )}
        </>
      ) : (
        // Loading spinner
        <div className="mx-auto text-center my-40 w-full">
          <BarLoader className="mx-auto" />
        </div>
      )}
    </div>
  );
};

export default RecipesResult;
