import "./Popup.css"
import {useEffect, useRef, useState} from "react";

const Popup = ({header, body, leftButtonText, rightButtonText, onResult}) => {

  const popupRef = useRef('');
  const [isOpen, setIsOpen] = useState(false)

  useEffect(() => {
    setIsOpen(true);
  }, []);

  useEffect(() => {
    const handler = (e) => {
      if (popupRef.current && !popupRef.current.contains(e.target)) {
        setIsOpen(false);
        onResult(false);
      }
    };
    document.addEventListener('mousedown', handler);

    return () => {
      document.removeEventListener('mousedown',handler);
    }
  }, [onResult]);



  return isOpen &&(
      <div className={"Popup"}>
        <div ref={popupRef} className={"popup-content"}>

          <div className={"popup-header"}>
            {header}
          </div>
          <div className={"popup-body"}>
            {body}
          </div>
          <div className={"popup-footer"}>
          {/* 버튼*/}
            <div className={"popup-leftButton"}>
              <button onClick={()=>{
                setIsOpen(false);
                onResult(false);
              }}>{leftButtonText ? leftButtonText: "취소"}</button>
            </div>
            <div className={"popup-rightButton"}>
              <button onClick={()=>{
                setIsOpen(false);
                onResult(true);
              }}>{rightButtonText ? rightButtonText : "확인"}</button>
            </div>
          </div>
        </div>
      </div>
  )
}

export default Popup