import "./Title.css"
import {useNavigate} from "react-router-dom";
import {useSearchContext} from "../../context/SearchContext.jsx";

const Title = () => {
  let nav = useNavigate();
  const {resetSearchCondition} = useSearchContext();
  const onClickTitle = () => {
    resetSearchCondition();
    nav("/");
  }

  return (
      <>
        <div
            className={"title"}
            onClick={onClickTitle}
        >SolveLog</div>
      </>
  )
}

export default Title;