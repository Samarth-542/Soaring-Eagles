import type { ImgHTMLAttributes } from 'react'

type ResponsiveImageProps = Omit<ImgHTMLAttributes<HTMLImageElement>, 'src'> & {
  src: string
}

function getWebpSource(src: string) {
  const [path, suffix = ''] = src.split(/(?=[?#])/, 2)
  if (!path.startsWith('/images/')) return null
  if (!/\.(?:png|jpe?g)$/i.test(path)) return null
  return `${encodeURI(path.replace(/\.(?:png|jpe?g)$/i, '.webp'))}${suffix}`
}

function ResponsiveImage({ src, alt, loading = 'lazy', decoding = 'async', ...props }: ResponsiveImageProps) {
  const webpSource = getWebpSource(src)

  return (
    <picture className="responsive-image">
      {webpSource && <source srcSet={webpSource} type="image/webp" />}
      <img src={src} alt={alt} loading={loading} decoding={decoding} {...props} />
    </picture>
  )
}

export default ResponsiveImage
