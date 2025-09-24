import {createContext, useContext, useRef, useState} from "react";
import Popup from "../components/common/Popup.jsx";

export const PopupContext = createContext();

export const PopupProvider = ({children}) => {
  const [popupOptions, setPopupOptions] = useState(null);
  const resolver = useRef(null);

  const confirm = ({header, body, leftButtonText, rightButtonText}) => {
    return new Promise((resolve) => {
      resolver.current = resolve
      setPopupOptions({
        header: header,
        body: body,
        leftButtonText: leftButtonText,
        rightButtonText: rightButtonText
      });
    });
  }

  const handleResult = (result) => {
    setPopupOptions(null);
    resolver.current?.(result);
  }

  return (
      <PopupContext.Provider value={{confirm}}>
        {children}
        {popupOptions && (
            <Popup
              header={popupOptions.header}
              body={popupOptions.body}
              leftButtonText={popupOptions.leftButtonText}
              rightButtonText={popupOptions.rightButtonText}
              onResult={handleResult}
            />
        )}
      </PopupContext.Provider>
  )
}

export const usePopup = () => {
  const popupContext = useContext(PopupContext);
  if (!popupContext) {
    throw new Error("usePopup PopupProvider 내부에서 사용되어야 합니다.")
  }
  return useContext(PopupContext).confirm;
}