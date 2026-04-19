from fastapi import APIRouter

from app.schemas.documents import DocumentAnalysisRequest, DocumentAnalysisResponse
from app.services.document_analysis_service import analyze_document


router = APIRouter(prefix="/documents", tags=["Documents"])


@router.post("/analyze", response_model=DocumentAnalysisResponse)
def analyze_document_route(request: DocumentAnalysisRequest) -> DocumentAnalysisResponse:
    return analyze_document(request)
