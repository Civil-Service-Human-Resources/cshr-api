SELECT count(*) FROM vacancies WHERE public_opening_date IS NOT NULL AND public_opening_date <= current_timestamp AND ((point(:searchFromLongitudeValue, :searchFromLatitudeValue) <@> point(longitude, latitude) < :distance) AND closing_date > current_timestamp