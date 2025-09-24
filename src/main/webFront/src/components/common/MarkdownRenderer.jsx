import "./MarkdownRenderer.css"
import remarkGfm from "remark-gfm";
import rehypeHighlight from "rehype-highlight";
import rehypeRaw from "rehype-raw";
import ReactMarkdown from "react-markdown";
import {memo} from "react";

const MarkdownRenderer = ({content, headingRenderer}) => {



  return headingRenderer ? (
      <ReactMarkdown
          remarkPlugins={[remarkGfm]}
          rehypePlugins={[rehypeHighlight, rehypeRaw]}
          components={{
            h1 : headingRenderer("h1"),
            h2 : headingRenderer("h2"),
            h3 : headingRenderer("h3"),
            h4 : headingRenderer("h4"),
            h5 : headingRenderer("h5"),
            h6 : headingRenderer("h7"),
            img: ({node, ...props}) => {
              return(<img
                  loading={"lazy"}
                  style={{maxWidth:'100%', display:'block', margin:'0 auto 3rem auto'}}
                  {...props}
              />);
            }
          }}
      >
        {content}
      </ReactMarkdown>
  ) : (
      <ReactMarkdown
          remarkPlugins={[remarkGfm]}
          rehypePlugins={[rehypeHighlight, rehypeRaw]}
          components={{
            img: ({node, ...props}) => {
              return(<img
                  loading={"lazy"}
                  style={{maxWidth:'100%', display:'block', margin:'0 auto 3rem auto'}}
                  {...props}
              />);
            }
          }}
      >
        {content}
      </ReactMarkdown>
  )


}

export default memo(MarkdownRenderer);