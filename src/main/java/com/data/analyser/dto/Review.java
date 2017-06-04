package com.data.analyser.dto;

import com.data.analyser.component.ReviewsFileDescriptor;

public class Review {

    String id;
    String productId;
    String userId;
    String userProfileName;
    String text;


    public Review(String csvLine) {
        //todo validate input, check nulls
        String[] parts = csvLine.split(ReviewsFileDescriptor.CSV_SPLIT_BY);
        this.id = parts[ReviewsFileDescriptor.ID];
        this.productId = parts[ReviewsFileDescriptor.PRODUCT_ID];
        this. userId = parts[ReviewsFileDescriptor.USER_ID];
        this. userProfileName = parts[ReviewsFileDescriptor.PROFILE_NAME];
        this. text = parts[ReviewsFileDescriptor.COMMENT_TEXT].toLowerCase();
    }

    public String getId() {
        return id;
    }

    public String getProductId() {
        return productId;
    }

    public String getUserId() {
        return userId;
    }

    public String getUserProfileName() {
        return userProfileName;
    }

    public String getText() {
        return text;
    }

    public ReviewId getReviewId(){
        return new ReviewId(productId, userId);
    }

    public static class ReviewId {
        String productId;
        String userId;

        public ReviewId(String productId, String userId) {
            this.productId = productId;
            this.userId = userId;
        }

        public String getProductId() {
            return productId;
        }

        public String getUserId() {
            return userId;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof ReviewId)) {
                return false;
            }

            ReviewId reviewId = (ReviewId) o;

            if (!productId.equals(reviewId.productId)) {
                return false;
            }
            return userId.equals(reviewId.userId);

        }

        @Override
        public int hashCode() {
            int result = productId.hashCode();
            result = 31 * result + userId.hashCode();
            return result;
        }
    }
}
