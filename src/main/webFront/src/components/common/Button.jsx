import "./Button.css"

export const Button1 = ({buttonText, buttonEvent}) => {
  return (
      <>
        <button className={"button1"}
            onClick={buttonEvent}
        >
          {buttonText}
        </button>
      </>
  )
}

export const Button2 = ({buttonText, buttonEvent, buttonType, disabled}) => {
  return (
      <>
        <button className={"button2"}
                type={buttonType}
                onClick={buttonEvent}
                disabled={disabled}
        >
          {buttonText}
        </button>
      </>
  )
}

// export default Button1;