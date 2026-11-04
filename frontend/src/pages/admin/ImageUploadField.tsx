import { useEffect, useState } from 'react'
import './ImageUploadField.css'

const maxImageSize = 10 * 1024 * 1024

interface ImageUploadFieldProps {
  label: string
  files: File[]
  onFilesChange: (files: File[]) => void
  currentUrls?: string[]
  onRemoveCurrent?: (url: string) => void
  multiple?: boolean
  required?: boolean
  disabled?: boolean
}

export default function ImageUploadField({
  label,
  files,
  onFilesChange,
  currentUrls = [],
  onRemoveCurrent,
  multiple = false,
  required = false,
  disabled = false,
}: ImageUploadFieldProps) {
  const [fileError, setFileError] = useState('')
  const [previews, setPreviews] = useState<string[]>([])

  useEffect(() => {
    const urls = files.map((file) => URL.createObjectURL(file))
    setPreviews(urls)
    return () => urls.forEach((url) => URL.revokeObjectURL(url))
  }, [files])

  const selectFiles = (selectedFiles: FileList | null) => {
    if (!selectedFiles) return
    const accepted: File[] = []
    let error = ''
    for (const file of Array.from(selectedFiles)) {
      if (!['image/jpeg', 'image/png'].includes(file.type)) {
        error = 'Choose a JPEG or PNG image.'
      } else if (file.size > maxImageSize) {
        error = 'Images must be 10 MB or smaller.'
      } else {
        accepted.push(file)
      }
    }

    if (multiple) {
      const combined = [...files, ...accepted]
      if (combined.length + currentUrls.length > 20) error = 'You can attach up to 20 images.'
      onFilesChange(combined.slice(0, Math.max(0, 20 - currentUrls.length)))
    } else {
      onFilesChange(accepted.slice(0, 1))
    }
    setFileError(error)
  }

  return (
    <div className="admin-image-upload">
      <label className="admin-image-upload-picker">
        <span>{label}{required ? ' *' : ''}</span>
        <input
          type="file"
          accept="image/jpeg,image/png,.jpg,.jpeg,.png"
          multiple={multiple}
          required={required && files.length === 0 && currentUrls.length === 0}
          disabled={disabled}
          onChange={(event) => {
            selectFiles(event.target.files)
            event.currentTarget.value = ''
          }}
        />
        <small>JPEG or PNG · max 10 MB per image</small>
      </label>

      {fileError && <p className="admin-image-upload-error" role="alert">{fileError}</p>}

      {(currentUrls.length > 0 || files.length > 0) && (
        <div className="admin-image-upload-previews">
          {currentUrls.map((url) => (
            <div className="admin-image-upload-preview" key={url}>
              <img src={url} alt="Current image" />
              {onRemoveCurrent && (
                <button type="button" disabled={disabled} onClick={() => onRemoveCurrent(url)}>
                  REMOVE
                </button>
              )}
            </div>
          ))}
          {files.map((file, index) => (
            <div className="admin-image-upload-preview" key={`${file.name}-${file.lastModified}-${index}`}>
              <img src={previews[index]} alt={`Selected: ${file.name}`} />
              <button
                type="button"
                disabled={disabled}
                onClick={() => onFilesChange(files.filter((_, fileIndex) => fileIndex !== index))}
              >
                REMOVE
              </button>
            </div>
          ))}
        </div>
      )}
    </div>
  )
}
