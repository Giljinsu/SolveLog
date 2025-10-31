import "./Categories.css"
import { useEffect, useState } from "react";
import useCategoryList from "../../hooks/useCategoryList.jsx";
import {useSearchContext} from "../../context/SearchContext.jsx";

const Categories = () => {
  const {categoryList:categories} = useCategoryList("SEARCH_CATEGORY") || [];
  const {setSearchInput, searchCondition} = useSearchContext();
  const selected = searchCondition.categoryType




  useEffect(() => {
    if (categories.length === 0) return;

    if (!searchCondition.categoryType) {
      setSearchInput("categoryType", categories[0].type);
    }

  }, [categories, searchCondition.categoryType]);



  return (
      <div className="Categories_menu">
        {categories && categories.map((category) => (
            <span
                key={category.categoryId}
                className={`category_menu category_menu_${selected === category.type ? "selected" : ""}`}
                id={category.categoryId}
                onClick={() => {
                  setSearchInput("categoryType", category.type);
                }}
            >
          {category.type}
        </span>
        ))}
      </div>
  );
};

export default Categories;