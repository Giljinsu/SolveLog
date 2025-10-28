import "./LoadingPopup.css"
import Spinner from '../../assets/LoadingSpinner.gif'


const LoadingPopup = () => {
  return (
      <div className={"loading-popup"}>
        <div className={"loading-content"}>
          <div className={"loading-content-text"}>
            잠시만 기다려 주세요.
          </div>
          <img className={"loading-content-img"}
               src={Spinner} alt={"로딩중"}/>
        </div>

      </div>
  )
}

export default LoadingPopup;