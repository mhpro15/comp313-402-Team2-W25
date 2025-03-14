import React from "react";
import Image from "next/image";
import { useRouter } from "next/navigation";

// UI components
import { Card, CardContent } from "@/components/ui/card";
import {
  Carousel,
  CarouselContent,
  CarouselItem,
  CarouselNext,
  CarouselPrevious,
} from "@/components/ui/carousel";
import { Tooltip, TooltipContent, TooltipTrigger } from "./ui/tooltip";

/*
  RecipeCarousel Component
  Renders a carousel for displaying recipe cards with images.
*/
const RecipeCarousel = ({ data }) => {
  const router = useRouter();

  return (
    <>
      {/* Section Title */}
      <h2 className="font-semibold text-2xl text-center p-2">
        Top Pick of the Week
      </h2>

      {/* Carousel Container */}
      <Carousel
        opts={{ align: "start" }}
        className="max-w-[280px] md:max-w-lg lg:max-w-3xl 2xl:max-w-[1000px] mx-auto"
      >
        <CarouselContent>
          {/* Iterate over data to create carousel items */}
          {data?.map((item, index) => (
            <CarouselItem key={index} className="md:basis-1/2 lg:basis-1/3">
              <Card className="border-none">
                <CardContent className="flex aspect-square items-center justify-center p-2">
                  <div className="relative w-[300px] h-[200px] rounded-md overflow-hidden">
                    <Tooltip>
                      <TooltipTrigger className="cursor-default">
                        <Image
                          unoptimized
                          src={
                            item?.thumbnailUrl ||
                            "https://www.themealdb.com/images/media/meals/58oia61564916529.jpg"
                          }
                          alt={item?.title}
                          fill
                          sizes="100%"
                          className="rounded-t-lg cursor-pointer"
                          onClick={() => router.push(`/recipe/${item.id}`)}
                        />
                      </TooltipTrigger>
                      <TooltipContent
                        className="font-semibold max-w-[230px]"
                        side="right"
                      >
                        {item?.title}
                      </TooltipContent>
                    </Tooltip>
                  </div>
                </CardContent>
              </Card>
            </CarouselItem>
          ))}
        </CarouselContent>

        {/* Carousel Navigation */}
        <CarouselPrevious className="hidden sm:flex mx-2" />
        <CarouselNext className="hidden sm:flex mx-2" />
      </Carousel>
    </>
  );
};

export default RecipeCarousel;
