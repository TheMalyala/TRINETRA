from fastapi import HTTPException, status


class TrinetraException(HTTPException):
    def __init__(self, detail: str, status_code: int = status.HTTP_400_BAD_REQUEST):
        super().__init__(status_code=status_code, detail=detail)


class EntityNotFoundException(TrinetraException):
    def __init__(self, entity_name: str, entity_id: str):
        super().__init__(
            detail=f"{entity_name} with id {entity_id} not found",
            status_code=status.HTTP_404_NOT_FOUND,
        )


class ConsentRequiredException(TrinetraException):
    def __init__(self, detail: str = "Active consent grant required to access this resource"):
        super().__init__(detail=detail, status_code=status.HTTP_403_FORBIDDEN)
