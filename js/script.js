document.addEventListener("DOMContentLoaded", () => {
    const myTextView = document.getElementById("myTextView");
    const text = myTextView.textContent;
    const textSize = parseInt(window.getComputedStyle(myTextView).fontSize);
    const pattern = /\[moji:([a-zA-Z0-9]+|http[s]?:\/\/[^\s]+)]/g;
    const placeholderImage = 'placeholder.png';
    const corsProxy = 'https://cors-anywhere.herokuapp.com/';

    let modifiedText = text.replace(pattern, (match, emoji) => {
        if (emoji.startsWith("http")) {
            const img = `<img src="${placeholderImage}" data-url="${emoji}" class="emoji" style="width:${textSize}px; height:${textSize}px;">`;
            return img;
        } else {
            return `<img src="${emoji}.png" class="emoji" style="width:${textSize}px; height:${textSize}px;">`;
        }
    });

    myTextView.innerHTML = modifiedText;

    document.querySelectorAll('img[data-url]').forEach(img => {
        const url = img.getAttribute('data-url');

        fetch(corsProxy + url)
            .then(response => {
                if (!response.ok) {
                    throw new Error('Network response was not ok');
                }
                return response.blob();
            })
            .then(blob => {
                const reader = new FileReader();
                reader.onloadend = () => {
                    img.src = reader.result;
                };
                reader.onerror = () => {
                    img.src = placeholderImage;
                };
                reader.readAsDataURL(blob);
            })
            .catch(error => {
                console.error('Failed to load image from URL:', error);
                img.src = placeholderImage;
            });
    });
});
