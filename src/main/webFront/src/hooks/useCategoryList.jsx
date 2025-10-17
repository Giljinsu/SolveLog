import {useEffect, useState} from "react";
import axios from "axios";

const useCategoryList = (parentCategory, reloadKey = 0) => {
  const [categoryList, setCategoryList] = useState([])
  const [loading, setLoading] = useState(true);

  // parentCategory
  // SEARCH_CATEGORY : 게시판 종류
  useEffect(() => {
    // 리턴 categoryId, type
    const getCategories = async () => {
      try {
        setLoading(true);
        const axiosResponse = await axios.get(
            `/api/getCategoryListByParentType/${parentCategory}`);
        setCategoryList(axiosResponse.data.data)
      } catch (e) {
        console.log(e)
      } finally {
        setLoading(false);
      }
    }

    getCategories();
  }, [parentCategory, reloadKey]);
  // useCategoryList

  // console.log(categoryList)
  return {categoryList, loading};
}


export default useCategoryList;